package com.ticketing.notification.messaging.event;

import java.time.Instant;

/**
 * Inbound event published by Payment on "payment-failed".
 */
public record PaymentFailedEvent(
        String bookingReference,
        Long bookingId,
        String reason,
        Instant occurredAt
) {
}
