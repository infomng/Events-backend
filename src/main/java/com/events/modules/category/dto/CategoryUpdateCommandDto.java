package com.events.modules.category.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating an existing category.
 * Only accessible by ADMIN users.
 */
public record CategoryUpdateCommandDto(
        @NotBlank(message = "Category name cannot be empty")
        String name,

        String description
) {
}
