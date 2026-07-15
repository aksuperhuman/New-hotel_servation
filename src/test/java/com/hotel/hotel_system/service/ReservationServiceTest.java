package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.ReservationRequest;
import com.hotel.hotel_system.exception.InvalidReservationStateException;
import com.hotel.hotel_system.exception.RoomUnavailableException;
import com.hotel.hotel_system.model.*;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private LockService lockService;
    @Mock
    private RLock rLock;

    private ReservationService reservationService;

    private Room room;
    private RoomType roomType;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, roomRepository, availabilityService, lockService);

        Hotel hotel = new Hotel();
        hotel.setId(1L);

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(BigDecimal.valueOf(100));

        room = new Room();
        room.setId(10L);
        room.setHotel(hotel);
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.AVAILABLE);

        request = new ReservationRequest();
        request.setRoomId(10L);
        request.setGuestName("Alice");
        request.setCheckInDate(LocalDate.of(2026, 8, 1));
        request.setCheckOutDate(LocalDate.of(2026, 8, 4)); // 3 nights

        lenient().when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        lenient().when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createReservation_succeeds_whenLockAcquiredAndRoomAvailable() {
        when(lockService.buildLockKey(10L, request.getCheckInDate(), request.getCheckOutDate())).thenReturn("lock-key");
        when(lockService.tryAcquire("lock-key")).thenReturn(rLock);
        when(availabilityService.isRoomAvailable(10L, request.getCheckInDate(), request.getCheckOutDate())).thenReturn(true);

        Reservation reservation = reservationService.createReservation(request, null);

        assertEquals(ReservationStatus.HELD, reservation.getStatus());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(reservation.getTotalPrice())); // 3 nights * 100
        assertNotNull(reservation.getHoldExpiresAt());
        verify(lockService, never()).release(anyString());
    }

    @Test
    void createReservation_throws409_whenRoomCurrentlyHeldByAnotherCustomer() {
        when(lockService.buildLockKey(10L, request.getCheckInDate(), request.getCheckOutDate())).thenReturn("lock-key");
        when(lockService.tryAcquire("lock-key")).thenReturn(null); // another customer holds it

        RoomUnavailableException ex = assertThrows(RoomUnavailableException.class,
                () -> reservationService.createReservation(request, null));

        assertEquals("Room currently held by another customer.", ex.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_releasesLockAndThrows_whenOverlapDetectedAfterLockAcquired() {
        when(lockService.buildLockKey(10L, request.getCheckInDate(), request.getCheckOutDate())).thenReturn("lock-key");
        when(lockService.tryAcquire("lock-key")).thenReturn(rLock);
        when(availabilityService.isRoomAvailable(10L, request.getCheckInDate(), request.getCheckOutDate())).thenReturn(false);

        assertThrows(RoomUnavailableException.class, () -> reservationService.createReservation(request, null));

        verify(lockService).release("lock-key");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_rejectsCheckOutNotAfterCheckIn() {
        request.setCheckOutDate(request.getCheckInDate());

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(request, null));
    }

    // ---- State machine ----

    @Test
    void heldToPaymentProcessing_isAllowed() {
        Reservation reservation = heldReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation updated = reservationService.moveToPaymentProcessing(1L);

        assertEquals(ReservationStatus.PAYMENT_PROCESSING, updated.getStatus());
    }

    @Test
    void paymentProcessingToConfirmed_isAllowed_andReleasesLock() {
        Reservation reservation = heldReservation();
        reservation.setStatus(ReservationStatus.PAYMENT_PROCESSING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(lockService.buildLockKey(anyLong(), any(), any())).thenReturn("lock-key");

        Reservation updated = reservationService.confirm(1L);

        assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
        verify(lockService).release("lock-key");
    }

    @Test
    void confirmedToPaymentProcessing_isRejected() {
        Reservation reservation = heldReservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStateException.class,
                () -> reservationService.moveToPaymentProcessing(1L));
    }

    @Test
    void heldDirectlyToConfirmed_isRejected_mustGoThroughPaymentProcessing() {
        Reservation reservation = heldReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStateException.class,
                () -> reservationService.confirm(1L));
    }

    @Test
    void releasedReservation_hasNoValidOutgoingTransitions() {
        Reservation reservation = heldReservation();
        reservation.setStatus(ReservationStatus.RELEASED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidReservationStateException.class,
                () -> reservationService.confirm(1L));
    }

    private Reservation heldReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRoom(room);
        reservation.setCheckInDate(LocalDate.of(2026, 8, 1));
        reservation.setCheckOutDate(LocalDate.of(2026, 8, 4));
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setHoldExpiresAt(java.time.Instant.now().plusSeconds(300));
        return reservation;
    }
}
