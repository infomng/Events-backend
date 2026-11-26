package com.events.common.config.ratelimit.annotation;

import com.events.modules.auth.exception.TooManyRequestException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    /** Adresse du client ; en dehors d'une requête HTTP (tâche planifiée, test), un quota commun. */
    static String clientKey() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return "no-request";
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object limit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        // Un quota par client et par endpoint : un quota global serait partagé par tous les utilisateurs,
        // et quelques appels suffiraient à bloquer tout le monde.
        String key = pjp.getSignature().toShortString() + "|" + clientKey();

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
