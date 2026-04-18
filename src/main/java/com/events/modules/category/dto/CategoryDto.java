package com.events.modules.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Category responses.
 */
public record CategoryDto(
        @NotNull
        UUID id,
        @NotBlank
        String name,
        @NotBlank
        String description){
}
