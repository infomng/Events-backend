package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiVersion,
        @NotBlank String frontendBaseUrl
) {
}

