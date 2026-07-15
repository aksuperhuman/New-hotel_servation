package com.hotel.hotel_system.service;

import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import com.hotel.hotel_system.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    private AvailabilityService availabilityService;

    private Room room;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(roomRepository, roomTypeRepository, reservationRepository);
        room = new Room();
        room.setId(1L);
        room.setStatus(RoomStatus.AVAILABLE);
    }

    @Test
    void roomIsAvailable_whenNoOverlappingReservationExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(false);

        boolean available = availabilityService.isRoomAvailable(1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertTrue(available);
    }

    @Test
    void roomIsUnavailable_whenOverlappingBlockingReservationExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(true);

        boolean available = availabilityService.isRoomAvailable(1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertFalse(available);
    }

    @Test
    void roomIsUnavailable_whenUnderMaintenance() {
        room.setStatus(RoomStatus.MAINTENANCE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        boolean available = availabilityService.isRoomAvailable(1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertFalse(available);
    }

    @Test
    void roomIsUnavailable_whenRoomDoesNotExist() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        boolean available = availabilityService.isRoomAvailable(99L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertFalse(available);
    }

    @Test
    void blockingStatusesOnly_containHeldConfirmedAndPaymentProcessing() {
        assertTrue(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.HELD));
        assertTrue(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.CONFIRMED));
        assertTrue(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.PAYMENT_PROCESSING));
        assertFalse(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.FAILED));
        assertFalse(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.RELEASED));
        assertFalse(AvailabilityService.BLOCKING_STATUSES.contains(com.hotel.hotel_system.model.ReservationStatus.CANCELLED));
    }
}
