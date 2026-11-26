package com.events.modules.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySalesDto(
        UUID priceCategoryId,
        String name,
        BigDecimal price,
        int capacity,
        long soldTickets,
        long heldTickets,
        int availableTickets,
        BigDecimal revenue
) {
}
