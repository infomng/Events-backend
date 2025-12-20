package com.events.modules.auth.refreshtoken.dto.mapper;


import com.events.modules.auth.refreshtoken.Entity.RefreshToken;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;

public class RefreshTokenMapper {
    private RefreshTokenMapper() { }

    public static RefreshTokenResponseDto toRefreshTokenResponseDto(RefreshToken token) {
        return RefreshTokenResponseDto.builder().refreshToken(token.getToken()).build();
    }
}
