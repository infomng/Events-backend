package com.events.modules.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GetBookingItemDto(
        UUID id,
        UUID priceCategoryId,
        String priceCategoryName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
