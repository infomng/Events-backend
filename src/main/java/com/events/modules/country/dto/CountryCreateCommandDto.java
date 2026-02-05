package com.events.modules.country.dto;

import com.events.modules.country.enumeration.PaysEnum;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new country.
 * Only accessible by ADMIN users.
 */
public record CountryCreateCommandDto(
        @NotNull(message = "PaysEnum cannot be null")
        PaysEnum paysEnum
) {
}
