package com.events.modules.auth.dto;

import lombok.Builder;

@Builder
public record AccessTokenDto(String accessToken) {
}
