package com.ticketing.payment.messaging.event;

import java.time.Instant;

public record PaymentFailedEvent(
        String bookingReference,
        Long bookingId,
        String reason,
        Instant occurredAt
) {
}
