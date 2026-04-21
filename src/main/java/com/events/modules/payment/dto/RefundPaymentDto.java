package com.events.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RefundPaymentDto(
        UUID paymentId,

        @NotBlank(message = "Refund reason is required")
        String reason
) {
}
