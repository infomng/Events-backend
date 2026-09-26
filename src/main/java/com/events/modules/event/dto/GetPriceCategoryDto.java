package com.events.modules.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record GetPriceCategoryDto(
    UUID id,
    @NotBlank(message = "Category name cannot be empty")
    String name,

    @NotNull(message = "Category price cannot be null")
    @DecimalMin(value = "0.00", message = "Category price must be non-negative")
    BigDecimal price,

    @NotNull(message = "Total tickets for this category cannot be null")
    Integer totalTickets,

    @NotNull(message = "Available tickets cannot be null")
    Integer availableTickets
) {
}
