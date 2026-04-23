package com.events.modules.payment.dto;

import com.events.modules.payment.enumeration.PaymentStatusEnum;

import java.util.UUID;

public record PaymentResponseDto(
        UUID paymentId,
        String paymentReference,
        PaymentStatusEnum status,
        String redirectUrl,      // URL to redirect for payment completion (could be null for Stripe)
        String clientSecret,     // Stripe PaymentIntent client secret
        String message
) {
}
