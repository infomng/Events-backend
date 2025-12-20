package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.mail")
@Validated
public record MailProperties(
        @NotBlank String host,
        @NotNull Integer port,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String protocol,
        @NotNull Properties properties
) {

    public record Properties(
            @NotNull Mail mail
    ) {

        public record Mail(
                @NotNull Smtp smtp

        ) {

            public record Smtp(
                    @NotNull Boolean auth,
                    @NotNull Boolean starttlsEnable
            ) {
            }
        }

    }
}
