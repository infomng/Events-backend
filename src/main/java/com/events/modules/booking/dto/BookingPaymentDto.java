package com.events.modules.booking.dto;

import com.events.modules.event.enumeration.BookingStatusEnum;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Données d'une réservation nécessaires au module paiement.
 * Le montant provient toujours de la base, jamais du client.
 */
public record BookingPaymentDto(
        UUID id,
        UUID userId,
        BigDecimal totalAmount,
        BookingStatusEnum status,
        String bookingReference
) {
}
