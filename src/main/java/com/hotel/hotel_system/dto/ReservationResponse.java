package com.hotel.hotel_system.dto;

import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ReservationResponse {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomNumber;
    private String roomTypeName;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant holdExpiresAt;

    public static ReservationResponse from(Reservation r) {
        ReservationResponse dto = new ReservationResponse();
        dto.id = r.getId();
        if (r.getHotel() != null) {
            dto.hotelId = r.getHotel().getId();
            dto.hotelName = r.getHotel().getName();
        }
        if (r.getRoom() != null) {
            dto.roomId = r.getRoom().getId();
            dto.roomNumber = r.getRoom().getRoomNumber();
            if (r.getRoom().getRoomType() != null) {
                dto.roomTypeName = r.getRoom().getRoomType().getName();
            }
        }
        dto.guestName = r.getGuestName();
        dto.checkInDate = r.getCheckInDate();
        dto.checkOutDate = r.getCheckOutDate();
        dto.numberOfGuests = r.getNumberOfGuests();
        dto.totalPrice = r.getTotalPrice();
        dto.status = r.getStatus();
        dto.createdAt = r.getCreatedAt();
        dto.holdExpiresAt = r.getHoldExpiresAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(Instant holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}
