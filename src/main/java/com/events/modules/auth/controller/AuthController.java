package com.events.modules.auth.controller;

import com.events.common.config.properties.JwtProperties;
import com.events.common.config.ratelimit.annotation.RateLimit;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.dto.LoginRequestDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.dto.ForgotPasswordRequestDto;
import com.events.modules.auth.dto.ResetPasswordRequestDto;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;
import com.events.modules.auth.refreshtoken.service.IRefreshTokenService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.common.result.Result;
import com.events.modules.auth.utils.SecurityUtils;
import com.events.modules.user.dto.GetUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    private final IRefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AuthenticationConverter authenticationConverter;

    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(@RequestBody @Valid RegisterCommandDto command) {
        return ResponseEntity.ok(Result.success(authService.register(command)));}

    @PostMapping("/login")
    public ResponseEntity<Result<AccessTokenDto>> login(HttpServletRequest request) {
        final var token = authenticationConverter.convert(request);
        final var accessToken = authService.login(new LoginRequestDto(
                token.getName(),
                token.getCredentials().toString()
        ));

        RefreshTokenResponseDto refreshToken = refreshTokenService.createRefreshToken(token.getName());

        ResponseCookie refreshCookie = SecurityUtils
                .getResponseCookie(refreshToken, jwtProperties.refreshToken().duration());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Result.success(accessToken));
    }

    @PostMapping("refresh-token")
    public ResponseEntity<Result<AccessTokenDto>> refresh(HttpServletRequest request) {
        return ResponseEntity.ok(Result.success(refreshTokenService.getAccessToken(request)));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Result<String>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(Result.success(authService.verifyEmail(token)));
    }

    @GetMapping("/resend-verification-email")
    public ResponseEntity<Result<String>> resendVerificationEmail(@RequestParam String email) {
        return ResponseEntity.ok(Result.success(authService.resendVerificationEmail(email)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Result<String>> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        return ResponseEntity.ok(Result.success(authService.forgotPassword(request)));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<Result<String>> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        return ResponseEntity.ok(Result.success(authService.resetPassword(request)));
    }

    @RateLimit(capacity = 3)
    @GetMapping("/profile")
    public ResponseEntity<Result<GetUserDto>> profile() {
        return ResponseEntity.ok(Result.success(authService.getCurrentUserDto()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        //TODO: invalidate refresh token in database, blacklist JWT tokens if necessary
        request.getSession().invalidate();
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }
}
