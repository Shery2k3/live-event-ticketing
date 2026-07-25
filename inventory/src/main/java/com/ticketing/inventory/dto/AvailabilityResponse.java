package com.ticketing.inventory.dto;

public record AvailabilityResponse(
        Long eventId,
        long available,
        long held,
        long booked
) {
}
