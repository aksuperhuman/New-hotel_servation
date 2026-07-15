package com.hotel.hotel_system.exception;

/**
 * Thrown when an illegal reservation state transition is attempted
 * (e.g. CONFIRMED -> PAYMENT_PROCESSING).
 */
public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(String message) {
        super(message);
    }
}
