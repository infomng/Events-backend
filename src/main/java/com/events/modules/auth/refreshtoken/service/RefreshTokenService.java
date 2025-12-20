package com.events.modules.auth.refreshtoken.service;

import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.exception.InvalidRefreshTokenException;
import com.events.modules.auth.refreshtoken.Entity.RefreshToken;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenResponseDto;
import com.events.modules.auth.refreshtoken.dto.mapper.RefreshTokenMapper;
import com.events.modules.auth.refreshtoken.repository.IRefreshTokenRepository;
import com.events.modules.auth.service.jwt.IJwtService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserService userService;
    private final IJwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshTokenResponseDto createRefreshToken(String email) {
        User user = userService.findByEmail(email);

        RefreshToken token = RefreshToken.builder()
                .expirationDate(Instant.now().plusMillis(jwtProperties.refreshToken().duration()))
                .user(user)
                .token(jwtService.generateRefreshToken(user))
                .build();

        RefreshToken savedToken = this.refreshTokenRepository.save(token);

        return RefreshTokenMapper.toRefreshTokenResponseDto(savedToken);
    }

    @Override
    public AccessTokenDto getAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
       if (cookies == null) {
           throw new InvalidRefreshTokenException();
       }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> Constants.REFRESH_TOKEN.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        RefreshToken token = verifyRefreshToken(refreshToken);

        User user = token.getUser();
        return new AccessTokenDto(jwtService.generateAccessToken(user));
    }

    private RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = this.refreshTokenRepository.findByToken(token)
                .orElseThrow(InvalidRefreshTokenException::new);

        if(refreshToken.isExpired()){
            refreshTokenRepository.deleteById(refreshToken.getId());
            throw new InvalidRefreshTokenException();
        }
        return refreshToken;
    }
}
