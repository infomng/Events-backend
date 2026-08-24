package com.events.modules.auth.refreshtoken.service;

import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;

public interface IRefreshTokenService {

    RefreshTokenDto createRefreshToken(String token);

    AccessTokenDto getAccessToken(String refreshToken);

    void deleteRefreshToken(String refreshToken);
}
