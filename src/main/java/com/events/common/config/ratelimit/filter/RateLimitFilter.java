package com.events.common.config.ratelimit.filter;

import com.events.modules.auth.exception.TooManyRequestException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quota global de requêtes par adresse IP, contre les abus. Il doit rester large : une page charge
 * plusieurs ressources, et derrière un proxy mal configuré tous les clients partagent la même IP.
 * Les endpoints sensibles (connexion, inscription...) ont en plus leur propre quota via @RateLimit.
 */
@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:300}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))))
                .build());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest http && isExempt(http)) {
            chain.doFilter(request, response);
            return;
        }
        if (resolveBucket(request.getRemoteAddr()).tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            throw new TooManyRequestException();
        }
    }

    /** Préflights CORS, sondes de santé et webhook Stripe (requêtes signées) ne sont pas comptés. */
    static boolean isExempt(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || uri.startsWith("/actuator/health")
                || uri.endsWith("/payments/webhook/stripe");
    }
}
