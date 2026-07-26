package com.ticketing.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Binds the payment.* keys from config-repo/payment-service.yaml.
 */
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /** Payments strictly above this amount are automatically declined by the fake gateway. */
    private BigDecimal autoDeclineAbove = new BigDecimal("1000.00");

    /** Simulated gateway processing latency, in milliseconds. */
    private long processingDelayMs = 500;

    public BigDecimal getAutoDeclineAbove() {
        return autoDeclineAbove;
    }

    public void setAutoDeclineAbove(BigDecimal autoDeclineAbove) {
        this.autoDeclineAbove = autoDeclineAbove;
    }

    public long getProcessingDelayMs() {
        return processingDelayMs;
    }

    public void setProcessingDelayMs(long processingDelayMs) {
        this.processingDelayMs = processingDelayMs;
    }
}
