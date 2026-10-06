package com.events.modules.auth.controller;

import com.events.TestContexts.controllers.ControllerTestContext;
import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.common.utils.string.StringUtils;
import com.events.modules.auth.dto.*;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;
import com.events.utils.TestUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController}.
 */

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends ControllerTestContext {

    private static final String BASE_URL = "/api/v1/auth";

    @BeforeEach
    void setUpAll() {
        when(jwtProperties.accessToken()).thenReturn(new JwtProperties.AccessToken(Constants.TOKEN, 900000L));
    }

    @Test
    void register() throws Exception {
        RegisterCommandDto command = getRegisterCommand();

        when(authService.register(any(RegisterCommandDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void login() throws Exception {   // GIVEN -----------------------------------

        // 1. Mock du token extrait de la request
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(Constants.JOHN_DOE, Constants.PASSWORD);

        when(authenticationConverter.convert(any(HttpServletRequest.class)))
                .thenReturn(authenticationToken);

        AccessTokenDto accessTokenDto = new AccessTokenDto(Constants.TOKEN);
        when(authService.login(any(LoginRequestDto.class))).thenReturn(accessTokenDto);

        RefreshTokenDto refreshToken = new RefreshTokenDto(Constants.TOKEN);
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        JwtProperties.RefreshToken refreshProps =
                new JwtProperties.RefreshToken(Constants.TOKEN, Constants.REFRESH_TOKEN_MIN_DURATION);

        when(jwtProperties.refreshToken()).thenReturn(refreshProps);

        // WHEN - THEN -----------------------------

        mockMvc.perform(
                post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.HEADER, Constants.HEADER)
                )
                .andExpect(status().isOk())

                // Cookie bien généré
                .andExpect(cookie().exists(Constants.ACCESS_TOKEN))
                .andExpect(cookie().exists(Constants.REFRESH_TOKEN))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(Constants.HTTP_ONLY)))

                // Body JSON du Result<AccessToken>
                // Remplacer l'assertion du body par la vérification de la valeur null
                .andExpect(jsonPath("$.value").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    void refresh() throws Exception {
        when(refreshTokenService.getAccessToken(any())).thenReturn(AccessTokenDto.builder().accessToken(Constants.TOKEN).build());
        when(jwtProperties.accessToken()).thenReturn(new JwtProperties.AccessToken(Constants.TOKEN, 900000L));

        mockMvc.perform(post(BASE_URL + "/refresh-token")
                        .cookie(new Cookie(Constants.REFRESH_TOKEN, Constants.TOKEN)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(Constants.ACCESS_TOKEN)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(Constants.HTTP_ONLY)))
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Matchers.nullValue()));
    }

    @Test
    void verifyEmail() throws Exception {
        when(authService.verifyEmail(anyString())).thenReturn(StringUtils.EMPTY);

        mockMvc.perform(get(BASE_URL + "/verify-email").param("token", "test_token"))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
        .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
        .andExpect(jsonPath("$.value").value(StringUtils.EMPTY));
    }

    @Test
    void resendVerificationEmail() throws Exception {
        when(authService.resendVerificationEmail(anyString())).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(get(BASE_URL + "/resend-verification-email")
                        .param(Constants.EMAIL, Constants.JOHN_DOE_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void forgotPassword() throws Exception {
        when(authService.forgotPassword(any(ForgotPasswordRequestDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(post(BASE_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequestDto(Constants.JOHN_DOE_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    /**
     * Unit tests for {@link AuthController#resetPassword(ResetPasswordRequestDto)}.
     */
    @Test
    void resetPassword() throws Exception {
        when(authService.resetPassword(any(ResetPasswordRequestDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(post(BASE_URL + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequestDto(Constants.TOKEN, Constants.PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void profile() throws Exception {
        when(authService.getCurrentUserDto()).thenReturn(TestUtils.getUserDto());

        mockMvc.perform(get(BASE_URL + "/profile"))
                .andExpect(status().isOk());
    }

    private static RegisterCommandDto getRegisterCommand() {
        return RegisterCommandDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .build();
    }
}
