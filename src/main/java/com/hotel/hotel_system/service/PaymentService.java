package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.PaymentRequest;
import com.hotel.hotel_system.dto.PaymentResponse;
import com.hotel.hotel_system.exception.PaymentFailedException;
import com.hotel.hotel_system.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment gateway integration. In production this would call
 * out to a real processor (Stripe, Braintree, etc.); here it validates
 * the request, ties the outcome back into the reservation state machine,
 * and always leaves the reservation in a terminal-for-this-step state
 * (CONFIRMED or FAILED).
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final ReservationService reservationService;

    public PaymentService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Reservation reservation = reservationService.getByIdOrThrow(request.getReservationId());

        if (reservation.getTotalPrice() != null
                && request.getAmount().compareTo(reservation.getTotalPrice()) != 0) {
            throw new PaymentFailedException(
                    "Payment amount %s does not match reservation total %s"
                            .formatted(request.getAmount(), reservation.getTotalPrice()));
        }

        // Move HELD -> PAYMENT_PROCESSING (validates the hold hasn't expired).
        reservationService.moveToPaymentProcessing(reservation.getId());
        log.info("Payment processing started reservationId={} amount={}", reservation.getId(), request.getAmount());

        boolean approved = simulateGatewayCall(request);
        String transactionId = UUID.randomUUID().toString();

        if (approved) {
            reservationService.confirm(reservation.getId());
            log.info("Payment success reservationId={} transactionId={}", reservation.getId(), transactionId);
            return new PaymentResponse(reservation.getId(), transactionId, true, "Payment approved");
        } else {
            reservationService.fail(reservation.getId());
            log.warn("Payment failure reservationId={} transactionId={}", reservation.getId(), transactionId);
            throw new PaymentFailedException("Payment was declined by the card issuer.");
        }
    }

    /**
     * Simulated authorization: fails only on an obviously invalid card
     * number so the happy path is exercised by default. Replace with a
     * real gateway SDK call when integrating a live processor.
     */
    private boolean simulateGatewayCall(PaymentRequest request) {
        String digitsOnly = request.getCardNumber().replaceAll("\\s+", "");
        return digitsOnly.length() >= 12 && request.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}
