package com.hotel.hotel_system.exception;

/**
 * Thrown when a room cannot be booked for the requested dates, either
 * because it is already reserved (date overlap) or because another
 * customer currently holds the distributed lock for the same
 * room/date-range.
 */
public class RoomUnavailableException extends RuntimeException {

    public RoomUnavailableException(String message) {
        super(message);
    }
}
