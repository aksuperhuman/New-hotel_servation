package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import com.hotel.hotel_system.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @PostMapping
    public Room addRoom(@Valid @RequestBody Room room) {
        return service.addRoom(room);
    }

    @GetMapping
    public List<Room> getRooms(@RequestParam(required = false) Long hotelId) {
        return hotelId == null ? service.getAllRooms() : service.getRoomsByHotel(hotelId);
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return service.getRoomById(id);
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
        return service.updateRoom(id, room);
    }

    /**
     * Admin maintenance blocking, e.g. { "status": "MAINTENANCE" }.
     */
    @PatchMapping("/{id}/status")
    public Room updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        RoomStatus status = RoomStatus.valueOf(body.get("status").toUpperCase());
        return service.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deleteRoom(@PathVariable Long id) {
        service.deleteRoom(id);
        return "Room deleted successfully";
    }
}
