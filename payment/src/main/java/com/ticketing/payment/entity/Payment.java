package com.ticketing.payment.entity;

import com.ticketing.payment.entity.base.BaseEntity;
import com.ticketing.payment.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payment_transaction_ref", columnNames = "transaction_ref"),
                @UniqueConstraint(name = "uq_payment_booking_reference", columnNames = "booking_reference")
        }
)
@Getter @Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Column(name = "transaction_ref", nullable = false, length = 64, updatable = false)
    private String transactionRef;

    @Column(name = "booking_reference", nullable = false, length = 64, updatable = false)
    private String bookingReference;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
