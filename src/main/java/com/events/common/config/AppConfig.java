package com.events.common.config;

import com.events.common.config.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DatasourceProperties.class,
        JpaProperties.class,
        OAuthProperties.class,
        MailProperties.class,
        JwtProperties.class,
        AppProperties.class,
        SupabaseProperties.class,
        ServerProperties.class,
})
public class AppConfig {
}

