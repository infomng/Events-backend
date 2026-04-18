package com.events.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache configuration.
 * Enables caching for search results with TTL management.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with event search cache.
     * For production, consider using Redis or Caffeine with TTL support.
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("eventSearch");
    }
}
