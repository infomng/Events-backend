package com.events.modules.ticket.dto;

import java.math.BigDecimal;

/**
 * Une ligne de billets à émettre : {@code quantity} billets d'une même catégorie de prix.
 */
public record TicketLineDto(
        String category,
        BigDecimal unitPrice,
        int quantity
) {
}
