package com.events.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@TestConfiguration
public class WebMvcTestConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return Optional::empty;
    }
}

