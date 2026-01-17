package com.events.modules.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CreatePaymentDto(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotBlank
        String currency,

        @NotNull
        UUID bookingId
) {
}
