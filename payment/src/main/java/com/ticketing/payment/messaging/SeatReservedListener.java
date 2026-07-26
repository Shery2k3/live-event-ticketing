package com.ticketing.payment.messaging;

import com.ticketing.payment.messaging.event.SeatReservedEvent;
import com.ticketing.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatReservedListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "seat-reserved")
    public void onSeatReserved(SeatReservedEvent event) {
        log.info("Received SeatReserved for booking {}", event.bookingReference());
        paymentService.processPayment(event);
    }
}
