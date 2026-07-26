package com.ticketing.event.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record EventRequest(
        @NotBlank String name,
        String description,
        @NotBlank String venue,
        @NotNull @Future Instant startsAt,
        @NotNull @Min(1) Integer totalCapacity,
        @NotNull @DecimalMin(value = "0.0", inclusive = true)BigDecimal ticketPrice
        ) {
}
