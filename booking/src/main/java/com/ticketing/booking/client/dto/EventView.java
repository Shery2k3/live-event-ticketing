package com.ticketing.booking.client.dto;

import java.math.BigDecimal;

public record EventView(
        Long id,
        String name,
        BigDecimal ticketPrice
) {
}