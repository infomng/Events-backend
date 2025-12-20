package com.events.modules.auth.controller;

import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.common.utils.string.StringUtils;
import com.events.modules.auth.dto.*;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;
import com.events.modules.auth.refreshtoken.service.IRefreshTokenService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.auth.service.jwt.impl.JwtService;
import com.events.modules.user.dto.GetUserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @MockBean
    private IRefreshTokenService refreshTokenService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationConverter authenticationConverter;

    @Value("${app.api-version}")
    private String apiVersion;

    private String authApiUrl;

    @BeforeEach
    void setUp() {
        authApiUrl = apiVersion + "/auth";
    }

    @Test
    void register() throws Exception {
        RegisterCommandDto command = getRegisterCommand();

        when(authService.register(any(RegisterCommandDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(post(authApiUrl + "/register")
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

        RefreshTokenResponseDto refreshToken = new RefreshTokenResponseDto(Constants.TOKEN);
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        JwtProperties.RefreshToken refreshProps =
                new JwtProperties.RefreshToken(Constants.TOKEN, Constants.REFRESH_TOKEN_MIN_DURATION);

        when(jwtProperties.refreshToken()).thenReturn(refreshProps);

        // WHEN - THEN -----------------------------

        mockMvc.perform(
                post(authApiUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(Constants.HEADER, Constants.HEADER)
                )
                .andExpect(status().isOk())

                // Cookie bien généré
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(Constants.REFRESH_TOKEN)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(Constants.HTTP_ONLY)))

                // Body JSON du Result<AccessToken>
                .andExpect(jsonPath("$.value.access_token").value(Constants.TOKEN))
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    void refresh() throws Exception {
        when(refreshTokenService.getAccessToken(any())).thenReturn(AccessTokenDto.builder().access_token(Constants.TOKEN).build());

        mockMvc.perform(post(authApiUrl + "/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value.access_token").value(Constants.TOKEN));
    }

    @Test
    void verifyEmail() throws Exception {
        when(authService.verifyEmail(anyString())).thenReturn(StringUtils.EMPTY);

        mockMvc.perform(get(authApiUrl + "/verify-email").param("token", "test_token"))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
        .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
        .andExpect(jsonPath("$.value").value(StringUtils.EMPTY));
    }

    @Test
    void resendVerificationEmail() throws Exception {
        when(authService.resendVerificationEmail(anyString())).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(get(authApiUrl + "/resend-verification-email")
                        .param(Constants.EMAIL, Constants.JOHN_DOE_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void forgotPassword() throws Exception {
        when(authService.forgotPassword(any(ForgotPasswordRequestDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(post(authApiUrl + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequestDto(Constants.JOHN_DOE_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void resetPassword() throws Exception {
        when(authService.resetPassword(any(ResetPasswordRequestDto.class))).thenReturn(Constants.TEST_MESSAGE);

        mockMvc.perform(get(authApiUrl + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequestDto(Constants.TOKEN, Constants.PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(Boolean.TRUE))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.value").value(Constants.TEST_MESSAGE));
    }

    @Test
    void profile() throws Exception {
        when(authService.getCurrentUserDto()).thenReturn(getUserDto());

        mockMvc.perform(get(authApiUrl + "/profile")
)                .andExpect(status().isOk());
    }

    private static GetUserDto getUserDto() {
        return GetUserDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .build();
    }

    private static RegisterCommandDto getRegisterCommand() {
        return RegisterCommandDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .build();
    }
}
