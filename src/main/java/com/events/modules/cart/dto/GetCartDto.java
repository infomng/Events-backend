package com.events.modules.cart.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record GetCartDto(UUID id,
                         UUID userId,
                         Integer totalItems,
                         BigDecimal totalPrice,
                         List<GetCartItemDto> items) {
}
