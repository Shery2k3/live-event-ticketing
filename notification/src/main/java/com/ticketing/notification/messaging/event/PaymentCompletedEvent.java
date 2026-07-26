package com.ticketing.notification.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Inbound event published by Payment on "payment-completed".
 */
public record PaymentCompletedEvent(
        String bookingReference,
        Long bookingId,
        Long paymentId,
        BigDecimal amount,
        Instant occurredAt
) {
}
