package com.events.modules.auth.service.jwt;

import com.events.modules.user.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.function.Function;

public interface IJwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String generateVerificationToken(String email);

  <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

  String extractUsername(String token);

  boolean isTokenValid(String token, UserDetails userDetails);

  boolean isTokenExpired(String token);
}
