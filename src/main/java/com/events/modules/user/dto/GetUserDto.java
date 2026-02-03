package com.events.modules.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetUserDto (
    String email,
    String fullName,
    boolean isVerified,
    UUID countryId,
    String countryName,
    String countryCode
) {
}