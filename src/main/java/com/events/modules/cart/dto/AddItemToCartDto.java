package com.events.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddItemToCartDto(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "Price category ID is required")
        UUID priceCategoryId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        String seatId
) {
}
