package com.events.common.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "server")
@Validated
public record ServerProperties(
        @NotNull Integer port
) {}
