package com.events.common.config.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "supabase")
@Validated
public record SupabaseProperties(
        @NotBlank String projectUrl,
        @NotBlank String apiKey,
        @NotBlank String serviceRoleKey,
        @NotBlank String bucketName,
        @NotBlank String storageObject,
        @NotBlank String storageObjectPublic
) {}
