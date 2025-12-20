package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.datasource")
@Validated
public record DatasourceProperties(
        @NotBlank String url,
        @NotBlank String username,
        @NotBlank String password
) {}
