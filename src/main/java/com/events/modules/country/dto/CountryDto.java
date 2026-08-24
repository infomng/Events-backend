package com.events.modules.country.dto;

import com.events.modules.country.enumeration.PaysEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Country responses.
 */
public record CountryDto(
        @NotNull
        UUID id,
        @NotBlank
        String name,
        @NotBlank
        String code,
        @NotNull
        PaysEnum paysEnum
) {
}
