package com.events.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefundPaymentDto(
        @NotNull(message = "Payment ID is required")
        UUID paymentId,

        @NotBlank(message = "Refund reason is required")
        String reason
) {
}
