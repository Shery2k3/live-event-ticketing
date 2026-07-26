package com.ticketing.booking.client.dto;

public record SeatView(
        Long id,
        Long eventId,
        String seatNumber,
        String status
) {
}