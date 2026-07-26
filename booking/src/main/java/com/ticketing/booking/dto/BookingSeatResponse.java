package com.ticketing.booking.dto;

import java.math.BigDecimal;

public record BookingSeatResponse(
        Long seatId,
        String seatNumber,
        BigDecimal price
) {
}
