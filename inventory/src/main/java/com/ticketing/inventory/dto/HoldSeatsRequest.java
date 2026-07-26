package com.ticketing.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record HoldSeatsRequest(
        @NotBlank String bookingReference,
        @NotEmpty List<Long> seatIds
) {
}