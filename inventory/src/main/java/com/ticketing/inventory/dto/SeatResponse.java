package com.ticketing.inventory.dto;

import com.ticketing.inventory.entity.enums.SeatStatus;

public record SeatResponse(
        Long id,
        Long eventId,
        String seatNumber,
        SeatStatus status
) {
}
