package com.hotel.hotel_system.service;

import com.hotel.hotel_system.exception.ResourceNotFoundException;
import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import com.hotel.hotel_system.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public Room addRoom(Room room) {
        return repository.save(room);
    }

    public List<Room> getAllRooms() {
        return repository.findAll();
    }

    public List<Room> getRoomsByHotel(Long hotelId) {
        return repository.findByHotelId(hotelId);
    }

    public Room getRoomById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    public Room updateRoom(Long id, Room newRoom) {
        Room existing = getRoomById(id);

        existing.setHotel(newRoom.getHotel());
        existing.setRoomType(newRoom.getRoomType());
        existing.setRoomNumber(newRoom.getRoomNumber());
        existing.setFloor(newRoom.getFloor());
        if (newRoom.getStatus() != null) {
            existing.setStatus(newRoom.getStatus());
        }

        return repository.save(existing);
    }

    /**
     * Admin maintenance blocking: flips a room's status so it drops out
     * of availability search results without needing to touch/cancel
     * any reservations.
     */
    public Room setStatus(Long id, RoomStatus status) {
        Room room = getRoomById(id);
        room.setStatus(status);
        Room saved = repository.save(room);
        log.info("Room status changed id={} status={}", id, status);
        return saved;
    }

    public void deleteRoom(Long id) {
        repository.deleteById(id);
    }
}
