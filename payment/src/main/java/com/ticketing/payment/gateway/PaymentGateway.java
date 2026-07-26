package com.ticketing.payment.gateway;

import java.math.BigDecimal;

/**
 * Abstraction over a payment processor. Swap FakePaymentGateway for a real
 * Stripe/PayPal adapter later without touching PaymentService.
 */
public interface PaymentGateway {
    ChargeResult charge(String bookingReference, BigDecimal amount);
}
