package com.events.TestContexts.controllers;

import com.events.common.config.properties.JwtProperties;
import com.events.modules.auth.refreshtoken.service.IRefreshTokenService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.auth.service.jwt.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


public abstract class ControllerTestContext {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ===== Common security & auth mocks =====

    @MockitoBean
    protected IAuthService authService;

    @MockitoBean
    protected IRefreshTokenService refreshTokenService;

    @MockitoBean
    protected JwtProperties jwtProperties;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserDetailsService userDetailsService;

    @MockitoBean
    protected AuthenticationConverter authenticationConverter;

    // ===== Utilities =====

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
