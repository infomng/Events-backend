package com.events.modules.auth.service.auth;

import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.dto.LoginRequestDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.dto.ForgotPasswordRequestDto;
import com.events.modules.auth.dto.ResetPasswordRequestDto;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.entity.User;

import java.util.Optional;

public interface IAuthService {
    AccessTokenDto login(LoginRequestDto request);

    String register(RegisterCommandDto request);

    String verifyEmail(String token);

    String resendVerificationEmail(String email);

    String forgotPassword(ForgotPasswordRequestDto request);

    String resetPassword(ResetPasswordRequestDto request);

    User getCurrentUser();

    /**
     * Utilisateur connecté, ou vide pour un visiteur anonyme. Contrairement à {@link #getCurrentUser()},
     * ne lève pas d'exception : utilisable dans une transaction sans la marquer en échec.
     */
    Optional<User> findCurrentUser();
    GetUserDto getCurrentUserDto();
}
