package com.credigo.backend.security.jwt;

import io.jsonwebtoken.*;
import javax.crypto.SecretKey;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  @Value("${app.jwt.secret}")
  private String jwtSecretString;

  @Value("${app.jwt.expiration-ms}")
  private long jwtExpirationMs;

  private SecretKey key;

  @PostConstruct
  public void init() {
    if (jwtSecretString == null || jwtSecretString.isBlank()) {
      throw new IllegalStateException("JWT secret is not configured or is blank.");
    }

    try {
      byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
      this.key = Keys.hmacShaKeyFor(keyBytes);
      log.info("JWT Secret Key initialized successfully.");
    } catch (Exception e) {
      log.error("Error initializing JWT Secret Key: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to initialize JWT key", e);
    }
  }

  public String generateToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
    String username = userPrincipal.getUsername();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    List<String> roles = userPrincipal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .expiration(expiryDate)
        .claim("roles", roles)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    if (key == null) {
      log.error("JWT validation failed: Secret Key is not initialized.");
      return false;
    }

    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
      return true;
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature: {}", ex.getMessage());
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token: {}", ex.getMessage());
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token: {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token: {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty: {}", ex.getMessage());
    }

    return false;
  }
}
