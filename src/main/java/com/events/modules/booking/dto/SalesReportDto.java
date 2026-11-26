package com.events.modules.booking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Tableau de bord des ventes d'un événement.
 * soldTickets : réservations payées (ou gratuites confirmées) ; heldTickets : bloqués en attente de paiement.
 */
public record SalesReportDto(
        UUID eventId,
        String eventName,
        int capacity,
        long soldTickets,
        long heldTickets,
        int availableTickets,
        double fillRatePercent,
        BigDecimal revenue,
        long confirmedBookings,
        long checkedIn,
        List<CategorySalesDto> categories
) {
}
