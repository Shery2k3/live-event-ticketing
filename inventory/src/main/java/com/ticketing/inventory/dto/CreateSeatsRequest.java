package com.ticketing.inventory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateSeatsRequest(
        @NotNull Long eventId,
        @NotEmpty List<String> seatNumbers
) {
}
