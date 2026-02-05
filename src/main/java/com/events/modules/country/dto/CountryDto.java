package com.events.modules.country.dto;

import com.events.modules.country.enumeration.PaysEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Country responses.
 */
public record CountryDto(
        UUID id,
        String name,
        String code,
        PaysEnum paysEnum,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
