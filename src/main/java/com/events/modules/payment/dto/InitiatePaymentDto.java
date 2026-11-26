package com.events.modules.payment.dto;

import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Demande d'initiation de paiement.
 * Le montant et la devise ne sont volontairement PAS fournis par le client :
 * ils sont calculés côté serveur à partir de la réservation.
 */
public record InitiatePaymentDto(
        @NotNull(message = "Booking ID is required")
        UUID bookingId,

        @NotNull(message = "Payment method is required")
        PaymentMethodEnum paymentMethod,

        @NotNull(message = "Payment gateway is required")
        PaymentGatewayEnum paymentGateway,

        String metadata // Additional metadata as JSON string
) {
}
