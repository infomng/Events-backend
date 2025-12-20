package com.events.modules.auth.refreshtoken.dto;

import lombok.Builder;

@Builder
public record RefreshTokenResponseDto(String refreshToken) {
}