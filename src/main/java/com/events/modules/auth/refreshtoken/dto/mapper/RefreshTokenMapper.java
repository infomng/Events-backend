package com.events.modules.auth.refreshtoken.dto.mapper;


import com.events.modules.auth.refreshtoken.Entity.RefreshToken;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;

public class RefreshTokenMapper {
    private RefreshTokenMapper() { }

    public static RefreshTokenDto toRefreshTokenResponseDto(RefreshToken token) {
        return RefreshTokenDto.builder().refreshToken(token.getToken()).build();
    }
}
