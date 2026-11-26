package com.events.common.config.ratelimit;

import com.events.common.config.ratelimit.annotation.RateLimit;
import com.events.common.config.ratelimit.annotation.RateLimitAspect;
import com.events.modules.auth.exception.TooManyRequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitAspectTest {

    private final RateLimitAspect aspect = new RateLimitAspect();
    private final RateLimit limitOfTwo = mock(RateLimit.class);
    private final ProceedingJoinPoint call = mock(ProceedingJoinPoint.class);

    RateLimitAspectTest() {
        when(limitOfTwo.capacity()).thenReturn(2);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("AuthController.login(..)");
        when(call.getSignature()).thenReturn(signature);
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void from(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void quotaIsPerClientNotSharedByEveryone() throws Throwable {
        from("10.0.0.1");
        aspect.limit(call, limitOfTwo);
        aspect.limit(call, limitOfTwo);
        assertThatThrownBy(() -> aspect.limit(call, limitOfTwo)).isInstanceOf(TooManyRequestException.class);

        // Un autre client garde tout son quota
        from("10.0.0.2");
        assertThatCode(() -> aspect.limit(call, limitOfTwo)).doesNotThrowAnyException();
    }
}
