package com.ticketing.notification.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Inbound event published by Booking on "booking-confirmed".
 */
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
