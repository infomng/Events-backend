package com.events.modules.user.dto;

import com.events.modules.user.enumeration.RoleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record GetUserDto(
        @NotNull
        UUID id,
        @NotBlank
        String email,
        @NotBlank
        String fullName,
        boolean isVerified,
        @NotNull
        UUID countryId,
        @NotBlank
        String countryName,
        @NotBlank
        String countryCode,
        @NotNull
        RoleEnum role
) {
}