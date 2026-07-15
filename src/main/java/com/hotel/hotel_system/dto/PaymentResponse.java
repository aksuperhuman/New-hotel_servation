package com.hotel.hotel_system.dto;

public class PaymentResponse {

    private Long reservationId;
    private String transactionId;
    private boolean success;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(Long reservationId, String transactionId, boolean success, String message) {
        this.reservationId = reservationId;
        this.transactionId = transactionId;
        this.success = success;
        this.message = message;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
