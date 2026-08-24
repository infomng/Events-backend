package com.events.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentDto(
        @NotBlank(message = "Payment reference is required")
        String paymentReference,

        @NotBlank(message = "Transaction ID is required")
        String transactionId,

        String gatewayResponse // Optional: response from payment gateway
) {
}
