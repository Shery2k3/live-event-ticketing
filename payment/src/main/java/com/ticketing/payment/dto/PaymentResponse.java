package com.ticketing.payment.dto;

import com.ticketing.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        String transactionRef,
        String bookingReference,
        Long bookingId,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
}
