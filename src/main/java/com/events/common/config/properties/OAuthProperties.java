package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Validated
public record OAuthProperties(
        Registration registration,
        Provider provider
) {

    public record Registration(Google google) {

        public record Google(
                @NotBlank String clientId,
                @NotBlank String clientSecret,
                List<String> scope
        ) {}
    }

    public record Provider(Google google) {

        public record Google(
                @NotBlank String authorizationUri,
                @NotBlank String tokenUri,
                @NotBlank String userInfoUri
        ) {}
    }
}

