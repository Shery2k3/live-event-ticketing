package com.ticketing.booking.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        String bookingReference,
        Long bookingId,
        Long paymentId,
        BigDecimal amount,
        Instant occurredAt
) {
}