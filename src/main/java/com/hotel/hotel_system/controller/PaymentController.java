package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.dto.PaymentRequest;
import com.hotel.hotel_system.dto.PaymentResponse;
import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.User;
import com.hotel.hotel_system.security.CurrentUserProvider;
import com.hotel.hotel_system.service.PaymentService;
import com.hotel.hotel_system.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final CurrentUserProvider currentUserProvider;

    public PaymentController(PaymentService paymentService,
                              ReservationService reservationService,
                              CurrentUserProvider currentUserProvider) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/charge")
    public PaymentResponse charge(@Valid @RequestBody PaymentRequest request) {
        assertOwnerOrAdmin(request.getReservationId());
        return paymentService.processPayment(request);
    }

    private void assertOwnerOrAdmin(Long reservationId) {
        User currentUser = currentUserProvider.getCurrentUserOrNull();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        if ("ADMIN".equals(currentUser.getRole().name())) {
            return;
        }
        Reservation reservation = reservationService.getByIdOrThrow(reservationId);
        boolean owns = reservation.getUser() != null && reservation.getUser().getId().equals(currentUser.getId());
        if (!owns) {
            throw new AccessDeniedException("You do not own this reservation.");
        }
    }
}
