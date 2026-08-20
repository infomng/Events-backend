package com.events.common.config;

import com.events.modules.event.dto.GetEventDto;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring Cache configuration.
 * Enables caching for search results with TTL management.
 */
@Configuration
@EnableCaching
public class CacheConfig {

//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager("CATEGORIES", "USER", "EVENT", "EVENTS");
//    }


    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .build();
    }

    /**
     * Configure cache manager with event search cache.
     * For production, consider using Redis or Caffeine with TTL support.
     */
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
//
//        ObjectMapper redisMapper = new ObjectMapper();
//
//        redisMapper.registerModule(new JavaTimeModule());
//        redisMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        redisMapper.activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder()
//                        .allowIfSubType("com.events")
//                        .build(),
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.WRAPPER_OBJECT);
//
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(redisMapper);
//
//
//        RedisCacheConfiguration config =
//                RedisCacheConfiguration.defaultCacheConfig()
//                        .entryTtl(Duration.ofMinutes(10))
//                        .serializeValuesWith(
//                                RedisSerializationContext.SerializationPair
//                                        .fromSerializer(serializer)
//                        );
//
//        return RedisCacheManager.builder(factory)
//                .cacheDefaults(config)
//                .build();
//    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(
//            RedisConnectionFactory connectionFactory
//    ) {
//
//        RedisTemplate<String, Object> template =
//                new RedisTemplate<>();
//
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(
//                new StringRedisSerializer()
//        );
//
//        template.setValueSerializer(jacksonConfig());
//
//        template.setHashKeySerializer(
//                new StringRedisSerializer()
//        );
//
//        template.setHashValueSerializer(jacksonConfig());
//
//        return template;
//    }
//
//    private Jackson2JsonRedisSerializer<GetEventDto> jacksonConfig(){
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//
//        return new Jackson2JsonRedisSerializer<>(mapper, GetEventDto.class);
//    }
}
