package com.ticketing.notification.messaging;

import com.ticketing.notification.entity.enums.NotificationType;
import com.ticketing.notification.messaging.event.BookingConfirmedEvent;
import com.ticketing.notification.messaging.event.PaymentCompletedEvent;
import com.ticketing.notification.messaging.event.PaymentFailedEvent;
import com.ticketing.notification.messaging.event.SeatReservedEvent;
import com.ticketing.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Subscribes to the saga topics and records a notification for each. Every
 * listener runs under the "notification-service" consumer group (set in config),
 * so this service gets its own independent copy of every event.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final NotificationMessageFactory messages;

    @KafkaListener(topics = "seat-reserved")
    public void onSeatReserved(SeatReservedEvent event) {
        notificationService.record(NotificationType.SEAT_RESERVED, event.bookingReference(), messages.seatReserved(event));
    }

    @KafkaListener(topics = "payment-completed")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        notificationService.record(NotificationType.PAYMENT_COMPLETED, event.bookingReference(), messages.paymentCompleted(event));
    }

    @KafkaListener(topics = "payment-failed")
    public void onPaymentFailed(PaymentFailedEvent event) {
        notificationService.record(NotificationType.PAYMENT_FAILED, event.bookingReference(), messages.paymentFailed(event));
    }

    @KafkaListener(topics = "booking-confirmed")
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        notificationService.record(NotificationType.BOOKING_CONFIRMED, event.bookingReference(), messages.bookingConfirmed(event));
    }
}
