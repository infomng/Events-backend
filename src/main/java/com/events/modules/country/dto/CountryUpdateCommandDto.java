package com.events.modules.country.dto;

import com.events.modules.country.enumeration.PaysEnum;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating an existing country.
 * Only accessible by ADMIN users.
 */
public record CountryUpdateCommandDto(
        @NotNull(message = "PaysEnum cannot be null")
        PaysEnum paysEnum
) {
}
