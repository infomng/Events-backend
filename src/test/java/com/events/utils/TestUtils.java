package com.events.utils;

import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.refreshtoken.Entity.RefreshToken;
import com.events.modules.user.entity.User;
import com.events.modules.user.enumeration.RoleEnum;

import java.time.Instant;
import java.util.UUID;

public class TestUtils {
    private TestUtils() {}

    public static RegisterCommandDto getRegisterCommandDto() {
        return RegisterCommandDto.builder()
                .fullName(Constants.JOHN_DOE)
                .email(Constants.JOHN_DOE_EMAIL)
                .password(Constants.JOHN_DOE_PASSWORD)
                .verificationToken(Constants.TOKEN)
                .build();
    }

    public static User getUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(Constants.JOHN_DOE_EMAIL)
                .role(RoleEnum.USER)
                .isVerified(false)
                .verificationToken(Constants.TOKEN)
                .build();
    }

    public static RefreshToken getRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .token(Constants.TOKEN)
                .expirationDate(Instant.now().plusMillis(Constants.REFRESH_TOKEN_MIN_DURATION))
                .build();
    }

    public static RefreshToken getExpiredRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .token(Constants.TOKEN)
                .expirationDate(Instant.now().minusMillis(Constants.REFRESH_TOKEN_MIN_DURATION))
                .build();
    }



    public static JwtProperties.RefreshToken getRefreshTokenProps() {
        return new JwtProperties.RefreshToken(Constants.TOKEN, Constants.REFRESH_TOKEN_MIN_DURATION);
    }
}
