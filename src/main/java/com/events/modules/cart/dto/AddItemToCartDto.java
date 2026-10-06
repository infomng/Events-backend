package com.events.modules.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AddItemToCartDto(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "Price category ID is required")
        UUID priceCategoryId,

        @NotNull(message = "Quantity is required")
        Integer quantity,

        String seatId
) {
}
