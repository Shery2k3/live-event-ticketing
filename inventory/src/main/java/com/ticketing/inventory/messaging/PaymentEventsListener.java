package com.ticketing.inventory.messaging;

import com.ticketing.inventory.messaging.event.PaymentCompletedEvent;
import com.ticketing.inventory.messaging.event.PaymentFailedEvent;
import com.ticketing.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventsListener {

    private final InventoryService inventoryService;

    // Saga step: payment succeeded -> promote HELD seats to BOOKED.
    @KafkaListener(topics = "payment-completed")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompleted for booking {}", event.bookingReference());
        inventoryService.confirmSeats(event.bookingReference());
    }

    // Saga compensation: payment failed -> release the HELD seats back to AVAILABLE.
    @KafkaListener(topics = "payment-failed")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailed for booking {}", event.bookingReference());
        inventoryService.releaseSeats(event.bookingReference());
    }
}
