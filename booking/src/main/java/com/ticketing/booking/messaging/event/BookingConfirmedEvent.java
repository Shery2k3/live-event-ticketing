package com.ticketing.booking.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingConfirmedEvent(
        String bookingReference,
        Long bookingId,
        Long userId,
        Long eventId,
        List<Long> seatIds,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}