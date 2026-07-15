package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    List<Room> findByRoomTypeId(Long roomTypeId);

    List<Room> findByRoomTypeIdAndStatus(Long roomTypeId, RoomStatus status);

    List<Room> findByHotelIdAndStatus(Long hotelId, RoomStatus status);
}
