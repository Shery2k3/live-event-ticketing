package com.ticketing.event.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EventResponse(
        Long id,
        String name,
        String description,
        String venue,
        Instant startsAt,
        Integer totalCapacity,
        BigDecimal ticketPrice,
        Instant createdAt,
        Instant updatedAt
) {
}
