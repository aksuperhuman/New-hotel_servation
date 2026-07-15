package com.hotel.hotel_system.service;

import com.hotel.hotel_system.exception.ResourceNotFoundException;
import com.hotel.hotel_system.model.RoomType;
import com.hotel.hotel_system.repository.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeService {

    private static final Logger log = LoggerFactory.getLogger(RoomTypeService.class);

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public RoomType addRoomType(RoomType roomType) {
        RoomType saved = roomTypeRepository.save(roomType);
        log.info("RoomType created id={} name={} hotelId={}", saved.getId(), saved.getName(),
                saved.getHotel() != null ? saved.getHotel().getId() : null);
        return saved;
    }

    public List<RoomType> getByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    public List<RoomType> getAll() {
        return roomTypeRepository.findAll();
    }

    public RoomType getById(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found: " + id));
    }

    public RoomType updateRoomType(Long id, RoomType newRoomType) {
        RoomType existing = getById(id);
        existing.setName(newRoomType.getName());
        existing.setDescription(newRoomType.getDescription());
        existing.setBasePrice(newRoomType.getBasePrice());
        existing.setMaxOccupancy(newRoomType.getMaxOccupancy());
        existing.setAmenities(newRoomType.getAmenities());
        return roomTypeRepository.save(existing);
    }

    public void deleteRoomType(Long id) {
        roomTypeRepository.delete(getById(id));
    }
}
