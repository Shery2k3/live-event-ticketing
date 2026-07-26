package com.ticketing.payment.gateway;

public record ChargeResult(
        boolean success,
        String transactionRef,
        String failureReason
) {
    public static ChargeResult ok(String transactionRef) {
        return new ChargeResult(true, transactionRef, null);
    }

    public static ChargeResult declined(String transactionRef, String reason) {
        return new ChargeResult(false, transactionRef, reason);
    }
}
