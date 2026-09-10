package com.events.modules.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GetCartItemDto(
        UUID id,
        UUID eventId,
        String eventName,
        UUID priceCategoryId,
        String priceCategoryName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
