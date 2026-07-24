package com.ticketing.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EventRequest(
        @NotBlank String name,
        String description,
        @NotBlank String venue,
        @NotNull @Future Instant startsAt,
        @NotNull @Min(1) Integer totalCapacity
) {
}
