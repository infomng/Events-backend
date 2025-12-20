package com.events.modules.auth.config.ratelimit.filter;

import com.events.common.utils.contants.Constants;
import com.events.modules.auth.exception.TooManyRequestException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String key) {

        return buckets.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(
                                Bandwidth.classic(
                                        Constants.RATE_LIMIT_REQUESTS,
                                        Refill.greedy(Constants.RATE_LIMIT_REQUESTS, Duration.ofMinutes(Constants.RATE_LIMIT_RESET_TIME))
                                )
                        )
                        .build()
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String ip = request.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            throw new TooManyRequestException();
        }
    }
}

