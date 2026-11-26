package com.events.common.config.ratelimit;

import com.events.common.config.ratelimit.filter.RateLimitFilter;
import com.events.modules.auth.exception.TooManyRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter(3);

    private void call(String method, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    void quotaIsPerIp() throws Exception {
        for (int i = 0; i < 3; i++) call("GET", "/api/v1/events", "10.0.0.1");

        assertThatThrownBy(() -> call("GET", "/api/v1/events", "10.0.0.1")).isInstanceOf(TooManyRequestException.class);
        assertThatCode(() -> call("GET", "/api/v1/events", "10.0.0.2")).doesNotThrowAnyException();
    }

    @Test
    void preflightHealthAndWebhookAreNotCounted() throws Exception {
        for (int i = 0; i < 10; i++) {
            call("OPTIONS", "/api/v1/events", "10.0.0.3");
            call("GET", "/actuator/health/readiness", "10.0.0.3");
            call("POST", "/api/v1/payments/webhook/stripe", "10.0.0.3");
        }

        assertThatCode(() -> call("GET", "/api/v1/events", "10.0.0.3")).doesNotThrowAnyException();
    }
}
