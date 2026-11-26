package com.events.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
public class SchedulingConfig {

    /** Horloge injectable : permet de tester les expirations sans attendre. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Tâches planifiées (expiration des réservations...). Désactivables avec app.scheduling.enabled=false. */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
    static class EnabledScheduling {
    }
}
