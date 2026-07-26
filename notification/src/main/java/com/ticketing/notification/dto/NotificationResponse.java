package com.ticketing.notification.dto;

import com.ticketing.notification.entity.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String eventType,
        String bookingReference,
        String message,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventType().name(),
                notification.getBookingReference(),
                notification.getMessage(),
                notification.getCreatedAt());
    }
}
