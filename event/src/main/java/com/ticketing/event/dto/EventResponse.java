package com.ticketing.event.dto;

import java.time.Instant;

public record EventResponse(
        Long id,
        String name,
        String description,
        String venue,
        Instant startsAt,
        Integer totalCapacity,
        Instant createdAt,
        Instant updatedAt
) {
}
