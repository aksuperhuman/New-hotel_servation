package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Date-overlap check per spec:
     *   existing.checkIn  < requested.checkOut
     *   AND existing.checkOut > requested.checkIn
     * Only reservations in a status that actually blocks the room
     * (HELD, CONFIRMED, PAYMENT_PROCESSING) are considered; FAILED,
     * RELEASED and CANCELLED reservations are ignored.
     */
    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.room.id = :roomId
        AND r.status IN :blockingStatuses
        AND r.checkInDate < :checkOutDate
        AND r.checkOutDate > :checkInDate
    """)
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("blockingStatuses") List<ReservationStatus> blockingStatuses
    );

    List<Reservation> findByStatusAndHoldExpiresAtBefore(ReservationStatus status, Instant instant);

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByHotelId(Long hotelId);

    List<Reservation> findByRoomId(Long roomId);
}
