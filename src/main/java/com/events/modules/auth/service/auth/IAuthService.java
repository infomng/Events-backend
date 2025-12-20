package com.events.modules.auth.service.auth;

import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.dto.LoginRequestDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.dto.ForgotPasswordRequestDto;
import com.events.modules.auth.dto.ResetPasswordRequestDto;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.entity.User;

public interface IAuthService {
    AccessTokenDto login(LoginRequestDto request);

    String register(RegisterCommandDto request);

    String verifyEmail(String token);

    String resendVerificationEmail(String email);

    String forgotPassword(ForgotPasswordRequestDto request);

    String resetPassword(ResetPasswordRequestDto request);

    User getCurrentUser();
    GetUserDto getCurrentUserDto();
}
