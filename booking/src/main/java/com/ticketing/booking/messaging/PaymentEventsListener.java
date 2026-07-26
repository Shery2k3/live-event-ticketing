package com.ticketing.booking.messaging;

import com.ticketing.booking.messaging.event.PaymentCompletedEvent;
import com.ticketing.booking.messaging.event.PaymentFailedEvent;
import com.ticketing.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventsListener {

    private final BookingService bookingService;

    @KafkaListener(topics = "payment-completed")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("<- PaymentCompleted {}", event.bookingReference());
        bookingService.handlePaymentCompleted(event);
    }

    @KafkaListener(topics = "payment-failed")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("<- PaymentFailed {}", event.bookingReference());
        bookingService.handlePaymentFailed(event);
    }
}