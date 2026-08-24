package com.events.modules.payment.dto;

import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentMethodEnum;
import com.events.modules.payment.enumeration.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GetPaymentDto(
        UUID id,
        UUID userId,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatusEnum status,
        PaymentMethodEnum paymentMethod,
        PaymentGatewayEnum paymentGateway,
        String transactionId,
        String paymentReference,
        LocalDateTime paidAt,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
