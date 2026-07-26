package com.ticketing.inventory.messaging.event;

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
