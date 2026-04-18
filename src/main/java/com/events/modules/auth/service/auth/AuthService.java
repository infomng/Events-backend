package com.events.modules.auth.service.auth;

import com.events.common.exception.BusinessException;
import com.events.common.utils.contants.Constants;
import com.events.common.utils.string.StringUtils;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.dto.LoginRequestDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.dto.ForgotPasswordRequestDto;
import com.events.modules.auth.dto.ResetPasswordRequestDto;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.dto.mapper.IUserMapper;
import com.events.modules.user.enumeration.RoleEnum;
import com.events.modules.auth.exception.EmailAlreadyExistException;
import com.events.modules.auth.exception.UserNotFoundException;
import com.events.common.mail.IMailService;
import com.events.modules.auth.service.jwt.IJwtService;
import com.events.common.exception.BadRequestException;
import com.events.modules.user.entity.User;

import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class AuthService implements IAuthService {

    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final IMailService emailService;
    private final IUserMapper userMapper;

    @Override
    public String register(RegisterCommandDto request) {

        try {
            if (userService.existsByEmail(request.email())) {
                throw new EmailAlreadyExistException(request.email());
            }

            String token = jwtService.generateVerificationToken(request.email());

            RegisterCommandDto command = RegisterCommandDto.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .fullName(request.fullName())
                    .verificationToken(token)
                    .role(RoleEnum.USER)
                    .build();

            userService.createUser(command);

            emailService.sendVerificationEmail(request.email(), token);

            return Constants.VERIFICATION_EMAIL_SENT_TO + request.email() + Constants.PLEASE_CHECK_YOUR_INBOX;
        } catch (EmailAlreadyExistException e) {
            throw e;
        }
        catch (Exception e) {
            throw new BusinessException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String verifyEmail(String token) {
        if(token == null || token.isEmpty()) {
            throw new BadRequestException(Constants.TOKEN_CANNOT_BE_NULL_OR_EMPTY);
        }

        String email = jwtService.extractUsername(token);

        User user = userService.findByEmail(email) ;

        if(user.isVerified()){
            throw  new BadRequestException(Constants.USER_ALREADY_VERIFIED);
        }

        if(user.getVerificationToken() == null || !user.getVerificationToken().equals(token)) {
            throw new BadRequestException(Constants.INVALID_VERIFICATION_TOKEN);
        }

        user.setVerified(Boolean.TRUE);
        user.setVerificationToken(null);


        return Constants.USER_HAS_BEEN_SUCCESSFULLY_VERIFIED + user.getFullName() + StringUtils.EMPTY + user.getEmail();
    }

    @Override
    public String resendVerificationEmail(String email) {
        User user = userService.findByEmail(email);

        if (user.isVerified()) {
            throw new BadRequestException(Constants.USER_ALREADY_VERIFIED);
        }

        String token = jwtService.generateVerificationToken(email);
        user.setVerificationToken(token);

        emailService.sendVerificationEmail(email, token);

        return Constants.VERIFICATION_EMAIL_SENT_TO + email + Constants.PLEASE_CHECK_YOUR_INBOX;
    }

    public AccessTokenDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userService.findByEmail(request.email());

        return jwtService.generateAccessToken(user);
    }

    @Override
    public String forgotPassword(ForgotPasswordRequestDto request) {
        User user = userService.findByEmail(request.email());

        String token = jwtService.generateVerificationToken(user.getEmail());
        user.setResetPasswordToken(token);
        emailService.sendResetPasswordEmail(user.getEmail(), token);
        return Constants.PASSWORD_RESET_EMAIL_SENT_TO + user.getEmail() + Constants.PLEASE_CHECK_YOUR_INBOX;
    }

    @Override
    public String resetPassword(ResetPasswordRequestDto request) {
        String email = jwtService.extractUsername(request.token());
        User user = userService.findByEmail(email);
        if(user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(request.token())) {
            throw new IllegalArgumentException(Constants.INVALID_OR_EXPIRED_TOKEN);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordToken(null);

        return Constants.PASSWORD_HAS_BEEN_RESET_SUCCESSFULLY_FOR + user.getEmail();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals(Constants.ANONYMOUS_USER)) {
            throw new UserNotFoundException(Constants.USER_NOT_AUTHENTICATED);
        }

        String email = authentication.getName();

        return userService.findByEmail(email);
    }

    @Override
    public GetUserDto getCurrentUserDto() {
        return userMapper.toGetUserDto(getCurrentUser());
    }
}
