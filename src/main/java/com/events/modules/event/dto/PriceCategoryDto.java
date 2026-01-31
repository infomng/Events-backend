package com.events.modules.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PriceCategoryDto(
    @NotBlank(message = "Category name cannot be empty")
    String name,

    @NotNull(message = "Category price cannot be null")
    @DecimalMin(value = "0.00", message = "Category price must be non-negative")
    BigDecimal price,

    @NotNull(message = "Total tickets for this category cannot be null")
    Integer totalTickets
) {
}
