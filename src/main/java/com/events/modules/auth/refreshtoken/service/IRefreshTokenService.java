package com.events.modules.auth.refreshtoken.service;

import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface IRefreshTokenService {

    RefreshTokenResponseDto createRefreshToken(String token);

    AccessTokenDto getAccessToken(HttpServletRequest request);
}
