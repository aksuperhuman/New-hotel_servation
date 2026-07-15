package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.ReservationRequest;
import com.hotel.hotel_system.exception.InvalidReservationStateException;
import com.hotel.hotel_system.exception.ReservationExpiredException;
import com.hotel.hotel_system.exception.ResourceNotFoundException;
import com.hotel.hotel_system.exception.RoomUnavailableException;
import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.ReservationStatus;
import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.User;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    /**
     * Reservation state machine (Feature 9):
     *   HELD -> PAYMENT_PROCESSING
     *   PAYMENT_PROCESSING -> CONFIRMED | FAILED
     *   HELD -> RELEASED (hold timeout) | CANCELLED (customer cancels before paying)
     *   CONFIRMED -> CANCELLED (cancellation after confirmation)
     * All other transitions are rejected.
     */
    private static final Map<ReservationStatus, Set<ReservationStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ReservationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ReservationStatus.HELD,
                EnumSet.of(ReservationStatus.PAYMENT_PROCESSING, ReservationStatus.RELEASED, ReservationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ReservationStatus.PAYMENT_PROCESSING,
                EnumSet.of(ReservationStatus.CONFIRMED, ReservationStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ReservationStatus.CONFIRMED,
                EnumSet.of(ReservationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(ReservationStatus.FAILED, EnumSet.noneOf(ReservationStatus.class));
        ALLOWED_TRANSITIONS.put(ReservationStatus.RELEASED, EnumSet.noneOf(ReservationStatus.class));
        ALLOWED_TRANSITIONS.put(ReservationStatus.CANCELLED, EnumSet.noneOf(ReservationStatus.class));
    }

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final AvailabilityService availabilityService;
    private final LockService lockService;

    public ReservationService(ReservationRepository reservationRepository,
                               RoomRepository roomRepository,
                               AvailabilityService availabilityService,
                               LockService lockService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.availabilityService = availabilityService;
        this.lockService = lockService;
    }

    /**
     * Booking flow step 1: acquire lock -> create reservation -> status HELD.
     * See LockService for why the lock key is roomId+checkIn+checkOut and
     * carries a 5 minute TTL.
     */
    @Transactional
    public Reservation createReservation(ReservationRequest request, User currentUser) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("checkOutDate must be after checkInDate");
        }

        String lockKey = lockService.buildLockKey(room.getId(), request.getCheckInDate(), request.getCheckOutDate());

        RLock lock = lockService.tryAcquire(lockKey);
        if (lock == null) {
            throw new RoomUnavailableException("Room currently held by another customer.");
        }

        try {
            if (!availabilityService.isRoomAvailable(room.getId(), request.getCheckInDate(), request.getCheckOutDate())) {
                lockService.release(lockKey);
                throw new RoomUnavailableException("Room is not available for the selected dates.");
            }

            Reservation reservation = new Reservation();
            reservation.setRoom(room);
            reservation.setHotel(room.getHotel());
            reservation.setUser(currentUser);
            reservation.setGuestName(request.getGuestName());
            reservation.setCheckInDate(request.getCheckInDate());
            reservation.setCheckOutDate(request.getCheckOutDate());
            reservation.setNumberOfGuests(request.getNumberOfGuests());
            reservation.setTotalPrice(calculateTotalPrice(room, request.getCheckInDate(), request.getCheckOutDate()));
            reservation.setStatus(ReservationStatus.HELD);
            reservation.setHoldExpiresAt(Instant.now().plusSeconds(LockService.LOCK_TTL_MINUTES * 60));

            Reservation saved = reservationRepository.save(reservation);
            log.info("Reservation created id={} roomId={} guest={} checkIn={} checkOut={} status=HELD",
                    saved.getId(), room.getId(), saved.getGuestName(), saved.getCheckInDate(), saved.getCheckOutDate());
            return saved;
        } catch (RoomUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            lockService.release(lockKey);
            throw e;
        }
    }

    private BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal basePrice = room.getRoomType() != null ? room.getRoomType().getBasePrice() : BigDecimal.ZERO;
        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    @Transactional
    public Reservation moveToPaymentProcessing(Long reservationId) {
        Reservation reservation = getByIdOrThrow(reservationId);
        assertNotExpired(reservation);
        return transition(reservation, ReservationStatus.PAYMENT_PROCESSING);
    }

    @Transactional
    public Reservation confirm(Long reservationId) {
        Reservation reservation = getByIdOrThrow(reservationId);
        Reservation updated = transition(reservation, ReservationStatus.CONFIRMED);
        releaseLockFor(updated);
        log.info("Reservation confirmed id={}", reservationId);
        return updated;
    }

    @Transactional
    public Reservation fail(Long reservationId) {
        Reservation reservation = getByIdOrThrow(reservationId);
        Reservation updated = transition(reservation, ReservationStatus.FAILED);
        releaseLockFor(updated);
        log.info("Reservation payment failed id={}", reservationId);
        return updated;
    }

    @Transactional
    public Reservation cancel(Long reservationId) {
        Reservation reservation = getByIdOrThrow(reservationId);
        Reservation updated = transition(reservation, ReservationStatus.CANCELLED);
        releaseLockFor(updated);
        log.info("Reservation cancelled id={}", reservationId);
        return updated;
    }

    /**
     * Generic status update entry point (kept for backward compatibility
     * with existing callers) - routes through the same state machine
     * validation as the explicit transition methods above.
     */
    @Transactional
    public Reservation updateStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = getByIdOrThrow(reservationId);
        Reservation updated = transition(reservation, status);
        if (status == ReservationStatus.CONFIRMED || status == ReservationStatus.FAILED || status == ReservationStatus.CANCELLED) {
            releaseLockFor(updated);
        }
        return updated;
    }

    private Reservation transition(Reservation reservation, ReservationStatus target) {
        ReservationStatus current = reservation.getStatus();
        Set<ReservationStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidReservationStateException(
                    "Cannot transition reservation %d from %s to %s".formatted(reservation.getId(), current, target));
        }
        reservation.setStatus(target);
        return reservationRepository.save(reservation);
    }

    private void assertNotExpired(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.HELD
                && reservation.getHoldExpiresAt() != null
                && reservation.getHoldExpiresAt().isBefore(Instant.now())) {
            throw new ReservationExpiredException(
                    "Hold for reservation " + reservation.getId() + " has expired.");
        }
    }

    private void releaseLockFor(Reservation reservation) {
        if (reservation.getRoom() == null) {
            return;
        }
        String lockKey = lockService.buildLockKey(
                reservation.getRoom().getId(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        lockService.release(lockKey);
    }

    /**
     * Scheduled sweep (Feature: automatic hold release). The Redisson
     * lock itself already expires after its TTL, but we also flip the
     * DB row to RELEASED so it stops showing up as HELD in the UI/API
     * even if this instance never receives another request for it.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredHolds() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndHoldExpiresAtBefore(ReservationStatus.HELD, Instant.now());
        for (Reservation reservation : expired) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
            releaseLockFor(reservation);
            log.info("Auto-released expired hold reservationId={}", reservation.getId());
        }
    }

    public Reservation getByIdOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getByHotel(Long hotelId) {
        return reservationRepository.findByHotelId(hotelId);
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return availabilityService.isRoomAvailable(roomId, checkInDate, checkOutDate);
    }
}
