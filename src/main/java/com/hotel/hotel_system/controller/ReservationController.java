package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.dto.AvailabilityRequest;
import com.hotel.hotel_system.dto.AvailabilityResponse;
import com.hotel.hotel_system.dto.ReservationRequest;
import com.hotel.hotel_system.dto.ReservationResponse;
import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.ReservationStatus;
import com.hotel.hotel_system.model.User;
import com.hotel.hotel_system.security.CurrentUserProvider;
import com.hotel.hotel_system.service.AvailabilityService;
import com.hotel.hotel_system.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService service;
    private final AvailabilityService availabilityService;
    private final CurrentUserProvider currentUserProvider;

    public ReservationController(ReservationService service,
                                  AvailabilityService availabilityService,
                                  CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.availabilityService = availabilityService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ReservationResponse addReservation(@Valid @RequestBody ReservationRequest request) {
        User currentUser = currentUserProvider.getCurrentUserOrNull();
        Reservation reservation = service.createReservation(request, currentUser);
        return ReservationResponse.from(reservation);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations(@RequestParam(required = false) Long hotelId,
                                                          @RequestParam(required = false) Long userId) {
        User currentUser = currentUserProvider.getCurrentUserOrNull();
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole().name());

        List<Reservation> reservations;
        if (!isAdmin) {
            // Customers may only ever see their own bookings, regardless
            // of what filters they pass in.
            if (currentUser == null) {
                throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
            }
            reservations = service.getByUser(currentUser.getId());
        } else if (hotelId != null) {
            reservations = service.getByHotel(hotelId);
        } else if (userId != null) {
            reservations = service.getByUser(userId);
        } else {
            reservations = service.getAllReservations();
        }
        return reservations.stream().map(ReservationResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id) {
        assertOwnerOrAdmin(id);
        return ReservationResponse.from(service.getByIdOrThrow(id));
    }

    @GetMapping("/availability")
    public boolean checkAvailability(
            @RequestParam Long roomId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        return service.isRoomAvailable(roomId, checkInDate, checkOutDate);
    }

    @PostMapping("/search")
    public List<AvailabilityResponse> searchAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.search(request);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        assertOwnerOrAdmin(id);
        return ReservationResponse.from(service.cancel(id));
    }

    private void assertOwnerOrAdmin(Long reservationId) {
        User currentUser = currentUserProvider.getCurrentUserOrNull();
        if (currentUser == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().name());
        if (isAdmin) {
            return;
        }
        Reservation reservation = service.getByIdOrThrow(reservationId);
        boolean owns = reservation.getUser() != null && reservation.getUser().getId().equals(currentUser.getId());
        if (!owns) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this reservation.");
        }
    }

    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        ReservationStatus status = ReservationStatus.valueOf(body.get("status").toUpperCase());
        return ReservationResponse.from(service.updateStatus(id, status));
    }
}
