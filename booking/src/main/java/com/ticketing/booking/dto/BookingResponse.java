package com.ticketing.booking.dto;

import com.ticketing.booking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        String bookingReference,
        Long userId,
        Long eventId,
        BookingStatus status,
        BigDecimal totalAmount,
        String failureReason,
        List<BookingSeatResponse> seats,
        Instant createdAt,
        Instant updatedAt
) {
}