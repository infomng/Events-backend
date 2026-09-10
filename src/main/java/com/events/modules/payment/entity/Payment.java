package com.events.modules.payment.entity;

import com.events.common.abstraction.AuditableEntity;
import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentMethodEnum;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Payment extends AuditableEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status = PaymentStatusEnum.PENDING;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentGatewayEnum paymentGateway;

    @Column(unique = true)
    private String transactionId; // ID from payment gateway

    @Column(unique = true)
    private String paymentReference; // Internal reference

    private String stripePaymentIntentId; // New field for Stripe Payment Intent ID
    private String stripeClientSecret; // New field for Stripe Client Secret

    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse; // JSON response from gateway

    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional metadata as JSON

    @PrePersist
    private void generatePaymentReference() {
        if (paymentReference == null) {
            paymentReference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public void markAsPaid(String transactionId) {
        this.status = PaymentStatusEnum.COMPLETED;
        this.transactionId = transactionId;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatusEnum.FAILED;
        this.failureReason = reason;
    }

    public void markAsRefunded() {
        this.status = PaymentStatusEnum.REFUNDED;
    }
}
