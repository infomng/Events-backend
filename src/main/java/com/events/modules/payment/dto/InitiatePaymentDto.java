package com.events.modules.payment.dto;

import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentMethodEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiatePaymentDto(
        @NotNull(message = "Booking ID is required")
        UUID bookingId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Payment method is required")
        PaymentMethodEnum paymentMethod,

        @NotNull(message = "Payment gateway is required")
        PaymentGatewayEnum paymentGateway,

        String returnUrl, // URL to redirect after payment

        String metadata // Additional metadata as JSON string
) {
}
