package com.ticketing.payment.messaging;

import com.ticketing.payment.config.KafkaConfig;
import com.ticketing.payment.messaging.event.PaymentCompletedEvent;
import com.ticketing.payment.messaging.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompleted for booking {}", event.bookingReference());
        kafkaTemplate.send(KafkaConfig.PAYMENT_COMPLETED, event.bookingReference(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailed for booking {}", event.bookingReference());
        kafkaTemplate.send(KafkaConfig.PAYMENT_FAILED, event.bookingReference(), event);
    }
}
