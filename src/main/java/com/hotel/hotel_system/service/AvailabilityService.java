package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.AvailabilityRequest;
import com.hotel.hotel_system.dto.AvailabilityResponse;
import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import com.hotel.hotel_system.model.RoomType;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import com.hotel.hotel_system.repository.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    /** Reservation statuses that make a room unavailable for the overlapping dates. */
    public static final Set<com.hotel.hotel_system.model.ReservationStatus> BLOCKING_STATUSES = Set.of(
            com.hotel.hotel_system.model.ReservationStatus.HELD,
            com.hotel.hotel_system.model.ReservationStatus.CONFIRMED,
            com.hotel.hotel_system.model.ReservationStatus.PAYMENT_PROCESSING
    );

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(RoomRepository roomRepository,
                                RoomTypeRepository roomTypeRepository,
                                ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * A single room is available for a date range if it's operational
     * (not under maintenance) and has no blocking reservation whose
     * dates overlap the requested range.
     */
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) {
            return false;
        }
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                roomId, checkIn, checkOut, List.copyOf(BLOCKING_STATUSES)
        );
        return !overlaps;
    }

    /**
     * Searches availability for a hotel (optionally scoped to a single
     * room type), grouped by room type, for the given date range.
     */
    public List<AvailabilityResponse> search(AvailabilityRequest request) {
        List<RoomType> roomTypes = request.getRoomTypeId() != null
                ? roomTypeRepository.findById(request.getRoomTypeId()).map(List::of).orElse(List.of())
                : roomTypeRepository.findByHotelId(request.getHotelId());

        List<AvailabilityResponse> results = new ArrayList<>();
        for (RoomType roomType : roomTypes) {
            List<Room> candidateRooms = roomRepository
                    .findByRoomTypeIdAndStatus(roomType.getId(), RoomStatus.AVAILABLE);

            List<Long> availableRoomIds = candidateRooms.stream()
                    .filter(room -> !reservationRepository.existsOverlappingReservation(
                            room.getId(), request.getCheckInDate(), request.getCheckOutDate(),
                            List.copyOf(BLOCKING_STATUSES)))
                    .map(Room::getId)
                    .collect(Collectors.toList());

            results.add(new AvailabilityResponse(
                    roomType.getId(),
                    roomType.getName(),
                    roomType.getBasePrice(),
                    availableRoomIds.size(),
                    availableRoomIds
            ));
        }

        log.info("Availability search hotelId={} checkIn={} checkOut={} -> {} room type(s)",
                request.getHotelId(), request.getCheckInDate(), request.getCheckOutDate(), results.size());

        return results;
    }
}
