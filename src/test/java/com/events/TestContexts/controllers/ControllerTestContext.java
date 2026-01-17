package com.events.TestContexts.controllers;

import com.events.common.config.properties.JwtProperties;
import com.events.modules.auth.refreshtoken.service.IRefreshTokenService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.auth.service.jwt.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;


public abstract class ControllerTestContext {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ===== Common security & auth mocks =====

    @MockBean
    protected IAuthService authService;

    @MockBean
    protected IRefreshTokenService refreshTokenService;

    @MockBean
    protected JwtProperties jwtProperties;

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected UserDetailsService userDetailsService;

    @MockBean
    protected AuthenticationConverter authenticationConverter;

    // ===== Utilities =====

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
