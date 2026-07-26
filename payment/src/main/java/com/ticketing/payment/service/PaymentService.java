package com.ticketing.payment.service;

import com.ticketing.payment.entity.Payment;
import com.ticketing.payment.entity.enums.PaymentStatus;
import com.ticketing.payment.gateway.ChargeResult;
import com.ticketing.payment.gateway.PaymentGateway;
import com.ticketing.payment.messaging.PaymentEventPublisher;
import com.ticketing.payment.messaging.event.PaymentCompletedEvent;
import com.ticketing.payment.messaging.event.PaymentFailedEvent;
import com.ticketing.payment.messaging.event.SeatReservedEvent;
import com.ticketing.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Handles a SeatReserved event by charging the customer, then emits the saga's
     * next event (completed/failed).
     *
     * Idempotent: a duplicate Kafka delivery for a booking we already processed is
     * skipped, so the customer is never double-charged.
     */
    @Transactional
    public void processPayment(SeatReservedEvent event) {
        if (paymentRepository.existsByBookingReference(event.bookingReference())) {
            log.info("Payment for booking {} already processed, skipping", event.bookingReference());
            return;
        }

        ChargeResult result = paymentGateway.charge(event.bookingReference(), event.amount());

        Payment payment = new Payment();
        payment.setTransactionRef(result.transactionRef());
        payment.setBookingReference(event.bookingReference());
        payment.setBookingId(event.bookingId());
        payment.setAmount(event.amount());

        if (result.success()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            Payment saved = paymentRepository.save(payment);
            eventPublisher.publishPaymentCompleted(new PaymentCompletedEvent(
                    saved.getBookingReference(),
                    saved.getBookingId(),
                    saved.getId(),
                    saved.getAmount(),
                    Instant.now()));
            log.info("Payment {} COMPLETED for booking {}", saved.getTransactionRef(), saved.getBookingReference());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
            Payment saved = paymentRepository.save(payment);
            eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    saved.getBookingReference(),
                    saved.getBookingId(),
                    saved.getFailureReason(),
                    Instant.now()));
            log.info("Payment {} FAILED for booking {}: {}",
                    saved.getTransactionRef(), saved.getBookingReference(), saved.getFailureReason());
        }
    }
}
