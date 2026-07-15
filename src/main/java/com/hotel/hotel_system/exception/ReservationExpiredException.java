package com.hotel.hotel_system.exception;

/**
 * Thrown when an action is attempted on a HELD reservation whose hold
 * window (holdExpiresAt) has already passed.
 */
public class ReservationExpiredException extends RuntimeException {

    public ReservationExpiredException(String message) {
        super(message);
    }
}
