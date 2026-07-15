package com.hotel.hotel_system.dto;

import java.math.BigDecimal;
import java.util.List;

public class AvailabilityResponse {

    private Long roomTypeId;
    private String roomTypeName;
    private BigDecimal basePrice;
    private int availableCount;
    private List<Long> availableRoomIds;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(Long roomTypeId, String roomTypeName, BigDecimal basePrice,
                                 int availableCount, List<Long> availableRoomIds) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.basePrice = basePrice;
        this.availableCount = availableCount;
        this.availableRoomIds = availableRoomIds;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public List<Long> getAvailableRoomIds() {
        return availableRoomIds;
    }

    public void setAvailableRoomIds(List<Long> availableRoomIds) {
        this.availableRoomIds = availableRoomIds;
    }
}
