package com.events.modules.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Passage de commande : les articles du panier de cet événement deviennent une réservation.
 */
public record CheckoutRequestDto(
        @NotNull(message = "Event ID is required")
        UUID eventId
) {
}
