package com.events.modules.auth.utils;

import com.events.common.exception.BadRequestException;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

public class SecurityUtils {
    private SecurityUtils() {
    }

    public static ResponseCookie getRefreshTokenCookie(RefreshTokenDto refreshToken, Long durationInMillis) {
        return ResponseCookie.from(Constants.REFRESH_TOKEN, refreshToken.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(durationInMillis))
                .sameSite("Strict")
                .build();
    }

    public static ResponseCookie getAccessTokenCookie(
            AccessTokenDto accessTokenDto,
            Long durationInMillis
    ) {
        return ResponseCookie.from(Constants.ACCESS_TOKEN, accessTokenDto.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(durationInMillis))
                .sameSite("Lax")
                .build();
    }

    public static String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, Constants.REFRESH_TOKEN);
        if (cookie == null) {
            throw new BadRequestException("Missing cookie");
        }

        String refreshToken = cookie.getValue();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Missing refresh token");
        }
        return refreshToken;
    }

    public static String extractAccessTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, Constants.ACCESS_TOKEN);
        if (cookie == null) {
            return null;
        }

        String refreshToken = cookie.getValue();

        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        return refreshToken;
    }
}
