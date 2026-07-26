package com.ticketing.payment.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Inbound event published by Booking. Field set MUST match Booking's producer
 * record exactly (same names) so Jackson can bind it.
 */
public record SeatReservedEvent(
        String bookingReference,
        Long bookingId,
        Long eventId,
        Long userId,
        List<Long> seatIds,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}
