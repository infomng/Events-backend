package com.events.modules.auth.service.auth;

import com.events.common.exception.BadRequestException;
import com.events.common.utils.contants.Constants;
import com.events.common.utils.string.StringUtils;
import com.events.modules.auth.dto.ForgotPasswordRequestDto;
import com.events.modules.auth.dto.LoginRequestDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.dto.ResetPasswordRequestDto;
import com.events.modules.auth.exception.EmailAlreadyExistException;
import com.events.modules.auth.service.jwt.IJwtService;
import com.events.modules.auth.service.mail.IMailService;
import com.events.modules.user.dto.mapper.IUserMapper;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IJwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private IUserService userService;
    @Mock
    private IMailService emailService;
    @Mock
    private IUserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldRegisterUserAndSendVerificationEmail() {
        // Given
        RegisterCommandDto registerCommand = RegisterCommandDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .verificationToken(Constants.TOKEN)
                .build();

        when(userService.existsByEmail(anyString())).thenReturn(Boolean.FALSE);
        when(passwordEncoder.encode(anyString())).thenReturn(Constants.JOHN_DOE_PASSWORD);
        when(jwtService.generateVerificationToken(anyString())).thenReturn(Constants.TOKEN);

        // When
        String result = authService.register(registerCommand);

        // Then
        verify(userService, times(1)).createUser(any(RegisterCommandDto.class));
        verify(emailService, times(1))
                .sendVerificationEmail(Constants.JOHN_DOE_EMAIL, Constants.TOKEN);
        assertTrue(result.contains(Constants.VERIFICATION_EMAIL_SENT_TO + Constants.JOHN_DOE_EMAIL + Constants.PLEASE_CHECK_YOUR_INBOX));
    }

    @Test
    void register_shouldThrowEmailAlreadyExistException() {
        // Given
        RegisterCommandDto registerCommand = RegisterCommandDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .verificationToken(Constants.TOKEN)
                .build();

        when(userService.existsByEmail(anyString())).thenReturn(Boolean.TRUE);

        // When & Then
        assertThrows(EmailAlreadyExistException.class, () -> authService.register(registerCommand));
    }

    @Test
    void verifyEmail_shouldVerifyUser() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();

        when(jwtService.extractUsername(Constants.TOKEN)).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);

        // When
        String result = authService.verifyEmail(Constants.TOKEN);

        // Then
        assertTrue(user.isVerified());
        assertNull(user.getVerificationToken());
        assertTrue(result.contains(Constants.USER_HAS_BEEN_SUCCESSFULLY_VERIFIED + user.getFullName() + StringUtils.EMPTY + user.getEmail()));
    }

    @Test
    void verifyEmail_shouldThrowBadRequestExceptionForAlreadyVerifiedUser() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        user.setVerified(Boolean.TRUE);

        when(jwtService.extractUsername(Constants.TOKEN)).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);

        // When & Then
        assertThrows(BadRequestException.class, () -> authService.verifyEmail(Constants.TOKEN));
    }

    @Test
    void verifyEmail_shouldThrowBadRequestExceptionForNullToken() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        user.setVerificationToken(null);

        when(jwtService.extractUsername(Constants.TOKEN)).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);

        // When & Then
        assertThrows(BadRequestException.class, () -> authService.verifyEmail(Constants.TOKEN));
    }

    @Test
    void verifyEmail_shouldThrowBadRequestExceptionForNonMatchingToken() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        user.setVerificationToken(StringUtils.EMPTY);

        when(jwtService.extractUsername(Constants.TOKEN)).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);

        // When & Then
        assertThrows(BadRequestException.class, () -> authService.verifyEmail(Constants.TOKEN));
    }

    @Test
    void resendVerificationEmail_shouldResendEmail() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        user.setVerificationToken(StringUtils.EMPTY);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(jwtService.generateVerificationToken(user.getEmail())).thenReturn(Constants.TOKEN);

        // When
        String result = authService.resendVerificationEmail(user.getEmail());

        // Then
        assertEquals(Constants.TOKEN, user.getVerificationToken());
        verify(emailService, times(1)).sendVerificationEmail(user.getEmail(), Constants.TOKEN);
        assertEquals(Constants.VERIFICATION_EMAIL_SENT_TO + user.getEmail() + Constants.PLEASE_CHECK_YOUR_INBOX, result);
    }

    @Test
    void login_shouldReturnAccessToken() {
        // Given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .build();

        User user = new User();
        user.setEmail(loginRequest.email());
        when(userService.findByEmail(loginRequest.email())).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(Constants.ACCESS_TOKEN);

        // When
        var result = authService.login(loginRequest);

        // Then
        assertEquals(Constants.ACCESS_TOKEN, result.access_token());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void forgotPassword_shouldSendResetPasswordEmail() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        ForgotPasswordRequestDto forgotPasswordRequest = new ForgotPasswordRequestDto(user.getEmail());
        when(userService.findByEmail(forgotPasswordRequest.email())).thenReturn(user);
        when(jwtService.generateVerificationToken(user.getEmail())).thenReturn(Constants.TOKEN);

        // When
        String result = authService.forgotPassword(forgotPasswordRequest);

        // Then
        verify(emailService, times(1)).sendResetPasswordEmail(user.getEmail(), Constants.TOKEN);
        assertEquals(Constants.PASSWORD_RESET_EMAIL_SENT_TO + user.getEmail() + Constants.PLEASE_CHECK_YOUR_INBOX, result);
    }

    @Test
    void resetPassword_shouldResetUserPassword() {
        // Given
        ResetPasswordRequestDto resetPasswordRequest = new ResetPasswordRequestDto(Constants.TOKEN, Constants.PASSWORD);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(com.events.modules.user.enumeration.RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
        user.setResetPasswordToken(Constants.TOKEN);

        when(jwtService.extractUsername(resetPasswordRequest.token())).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.encode(resetPasswordRequest.newPassword())).thenReturn(Constants.PASSWORD);

        // When
        String result = authService.resetPassword(resetPasswordRequest);

        // Then
        assertNull(user.getResetPasswordToken());
        assertEquals(Constants.PASSWORD, user.getPassword());
        assertTrue(result.contains(Constants.PASSWORD_HAS_BEEN_RESET_SUCCESSFULLY_FOR + user.getEmail()));
    }

}
