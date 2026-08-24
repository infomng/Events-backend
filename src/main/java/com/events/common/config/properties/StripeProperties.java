package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "stripe")
@Validated
public record StripeProperties(
        @NotBlank String publicKey,
        @NotBlank String secretKey
) {
}
