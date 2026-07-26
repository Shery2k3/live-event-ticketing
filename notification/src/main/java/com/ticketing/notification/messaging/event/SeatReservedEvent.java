package com.ticketing.notification.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Inbound event published by Booking on "seat-reserved". Field names MUST match
 * the producer record exactly so Jackson can bind it.
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
