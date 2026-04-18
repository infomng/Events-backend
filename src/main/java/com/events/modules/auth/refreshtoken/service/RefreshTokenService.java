package com.events.modules.auth.refreshtoken.service;

import com.events.common.config.properties.JwtProperties;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.exception.InvalidRefreshTokenException;
import com.events.modules.auth.refreshtoken.Entity.RefreshToken;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;
import com.events.modules.auth.refreshtoken.dto.mapper.RefreshTokenMapper;
import com.events.modules.auth.refreshtoken.repository.IRefreshTokenRepository;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.auth.service.jwt.IJwtService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserService userService;
    private final IJwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshTokenDto createRefreshToken(String email) {
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
    public AccessTokenDto getAccessToken(String refreshToken) {
        RefreshToken token = verifyRefreshToken(refreshToken);

        User user = token.getUser();
        return jwtService.generateAccessToken(user);
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
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
