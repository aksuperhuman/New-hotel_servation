package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.model.RoomType;
import com.hotel.hotel_system.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room-types")
@CrossOrigin(origins = "*")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    public RoomType addRoomType(@Valid @RequestBody RoomType roomType) {
        return roomTypeService.addRoomType(roomType);
    }

    @GetMapping
    public List<RoomType> getRoomTypes(@RequestParam(required = false) Long hotelId) {
        return hotelId == null ? roomTypeService.getAll() : roomTypeService.getByHotel(hotelId);
    }

    @GetMapping("/{id}")
    public RoomType getRoomType(@PathVariable Long id) {
        return roomTypeService.getById(id);
    }

    @PutMapping("/{id}")
    public RoomType updateRoomType(@PathVariable Long id, @Valid @RequestBody RoomType roomType) {
        return roomTypeService.updateRoomType(id, roomType);
    }

    @DeleteMapping("/{id}")
    public String deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return "Room type deleted successfully";
    }
}
