package com.events.modules.auth.config;

import com.events.common.config.properties.AppProperties;
import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.exception.ExceptionHandlerFilter;
import com.events.modules.auth.refreshtoken.dto.RefreshTokenDto;
import com.events.modules.auth.refreshtoken.service.IRefreshTokenService;
import com.events.modules.auth.service.jwt.impl.JwtAuthenticationFilter;
import com.events.modules.auth.service.jwt.impl.JwtService;
import com.events.modules.auth.service.oauth.CustomOAuth2UserService;
import com.events.modules.auth.utils.SecurityUtils;
import com.events.modules.user.entity.User;
import com.events.modules.user.enumeration.RoleEnum;
import com.events.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/api/v1/countries",
            "/api/v1/countries/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/oauth2/**", "/login/oauth2/**",
            "/error"
    };

    @Value("${app.api-version}")
    private String apiVersion;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final UserService userService;
    private final IRefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;

    // Chaîne de sécurité
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .requestMatchers(apiVersion + "/products/**").hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.USER.name())
                        .requestMatchers(apiVersion + "/auth/profile").authenticated()
                        .anyRequest()
                        .authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute(Constants.EMAIL);
                            String name = oAuth2User.getAttribute(Constants.NAME);

                            if (!userService.existsByEmail(email)) {

                                RegisterCommandDto command = RegisterCommandDto.builder()
                                        .fullName(name)
                                        .email(email)
                                        .password(Constants.EMPTY_STRING)
                                        .role(RoleEnum.USER)
                                        .build();

                                userService.createUser(command);
                            }

                            User user = userService.findByEmail(email);
                            AccessTokenDto accessTokenDto = jwtService.generateAccessToken(user);

                            RefreshTokenDto refreshToken = refreshTokenService.createRefreshToken(email);

                            ResponseCookie accessCookie = SecurityUtils
                                    .getAccessTokenCookie(
                                            accessTokenDto,
                                            jwtProperties.accessToken().duration()
                                    );

                            ResponseCookie refreshCookie = SecurityUtils
                                    .getRefreshTokenCookie(
                                            refreshToken,
                                            jwtProperties.refreshToken().duration()
                                    );

                            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
                            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                            response.sendRedirect(appProperties.frontendUrl() + "/home");

//                            response.sendRedirect(appProperties.frontendUrl() + "/oauth-success?token=" + token);
                        })
                );

        return http.build();
    }

    // Provider qui utilise notre service pour charger les users + encoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Pour utiliser AuthenticationManager dans AuthService
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Pour hasher les mots de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationConverter authenticationConverter() {
        return new BasicAuthenticationConverter();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

