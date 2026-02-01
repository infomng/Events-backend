package com.events.modules.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Category responses.
 */
public record CategoryDto(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
