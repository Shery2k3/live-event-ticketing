package com.ticketing.payment.gateway;

import com.ticketing.payment.config.PaymentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakePaymentGateway implements PaymentGateway {

    private final PaymentProperties properties;

    @Override
    public ChargeResult charge(String bookingReference, BigDecimal amount) {
        simulateLatency();

        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        if (amount.compareTo(properties.getAutoDeclineAbove()) > 0) {
            log.info("Charge DECLINED for booking {} (amount {} exceeds limit {})",
                    bookingReference, amount, properties.getAutoDeclineAbove());
            return ChargeResult.declined(transactionRef,
                    "Amount %s exceeds the per-transaction limit of %s"
                            .formatted(amount, properties.getAutoDeclineAbove()));
        }

        log.info("Charge APPROVED for booking {} (amount {}) -> {}", bookingReference, amount, transactionRef);
        return ChargeResult.ok(transactionRef);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(properties.getProcessingDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
