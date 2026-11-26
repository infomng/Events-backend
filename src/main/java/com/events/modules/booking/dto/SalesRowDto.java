package com.events.modules.booking.dto;

import com.events.modules.event.enumeration.BookingStatusEnum;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Agrégat des lignes de réservation par catégorie de prix et statut (projection JPQL).
 */
public record SalesRowDto(
        UUID priceCategoryId,
        BookingStatusEnum status,
        Long quantity,
        BigDecimal amount
) {
}
