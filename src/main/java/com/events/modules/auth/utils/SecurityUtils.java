package com.events.modules.auth.utils;

import com.events.common.utils.contants.Constants;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class SecurityUtils {
    private SecurityUtils() {}

    public static ResponseCookie getResponseCookie(RefreshTokenResponseDto refreshToken, Long durationInMillis) {
        return ResponseCookie.from(Constants.REFRESH_TOKEN, refreshToken.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(durationInMillis))
                .sameSite("Strict")
                .build();
    }
}
