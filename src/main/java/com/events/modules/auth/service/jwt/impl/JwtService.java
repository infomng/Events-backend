package com.events.modules.auth.service.jwt.impl;

import com.events.common.config.properties.JwtProperties;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.AccessTokenDto;
import com.events.modules.auth.service.jwt.IJwtService;
import com.events.modules.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtService implements IJwtService {

    private final JwtProperties jwtProperties;

    // Clé secrète (convertie en clé HMAC)
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.accessToken().secret().getBytes());
    }

    private String generateToken(User user, Long jwtExpiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.FULL_NAME, user.getFullName());
        claims.put(Constants.ROLE, user.getRole().name());
        claims.put(Constants.EMAIL, user.getEmail());

        return Jwts.builder()
                .subject(user.getId().toString()) // équivalent de setSubject()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSecretKey()) // sans algo ici
                .compact();
}

    @Override
    public AccessTokenDto generateAccessToken(User user) {
        return new AccessTokenDto(this.generateToken(user, jwtProperties.accessToken().duration()));
    }

    @Override
    public String generateRefreshToken(User user) {
        return this.generateToken(user, jwtProperties.refreshToken().duration());
    }

    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(Constants.EMAIL, email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessToken().duration()))
                .signWith(getSecretKey())
                .compact();
    }

    // Extraire un claim spécifique (ex: username/email)
 public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
         final Claims claims = extractAllClaims(token);
         return claimsResolver.apply(claims);
 }

    // Extraire l’email (subject)
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get(Constants.EMAIL, String.class));
    }

    // Vérifier si un token est valide pour un utilisateur
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Vérifier expiration
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Récupérer date d’expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Récupérer tous les claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
