package com.events.modules.user.dto;

import lombok.Builder;

@Builder
public record GetUserDto (
    String email,
    String fullName,
    boolean isVerified
) {
}