package com.events.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.jpa")
@Validated
public record JpaProperties(
        @NotNull Boolean showSql,
        @NotNull Hibernate hibernate,
        @NotNull Properties properties
) {

    public record Hibernate(
            @NotBlank String ddlAuto
    ) {}

    public record Properties(
            @NotNull HibernateDialect hibernate
    ) {

        public record HibernateDialect(
                @NotBlank String dialect,
                @NotNull Boolean formatSql
        ) {}
    }
}

