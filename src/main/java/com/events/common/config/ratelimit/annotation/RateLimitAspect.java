package com.events.common.config.ratelimit.annotation;

import com.events.modules.auth.exception.TooManyRequestException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object limit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        String key = pjp.getSignature().toShortString();

        int capacity = rateLimit.capacity();

        Bucket bucket = buckets.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(
                                Bandwidth.classic(
                                        capacity,
                                        Refill.intervally(capacity, Duration.ofMinutes(1))
                                )
                        )
                        .build()
        );

        if (bucket.tryConsume(1)) {
            return pjp.proceed();
        } else {
            throw new TooManyRequestException();
        }
    }
}
