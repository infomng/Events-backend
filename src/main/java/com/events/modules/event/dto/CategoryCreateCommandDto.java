package com.events.modules.event.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new category.
 * Only accessible by ADMIN users.
 */
public record CategoryCreateCommandDto(
        @NotBlank(message = "Category name cannot be empty")
        String name,

        String description
) {
}
