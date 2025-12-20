package com.events.common.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
        @NotBlank String secret,
        @Min(900000) Long duration, // minimum 15 minutes
        RefreshToken refreshToken
) {
    public record RefreshToken(@NotBlank String secret,
                                @Min(604800000) Long duration // minimum 7 days
    ){}

}

