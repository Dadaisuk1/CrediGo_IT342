package com.credigo.backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  @Value("${app.jwt.secret}")
  private String jwtSecretString;

  @Value("${app.jwt.expiration-ms}")
  private int jwtExpirationMs;

  private SecretKey key;

  @PostConstruct
  public void init() {
    try {
      byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
      this.key = Keys.hmacShaKeyFor(keyBytes);
      log.info("JWT Secret Key initialized successfully.");
    } catch (Exception e) {
      // Consider re-throwing or handling more robustly
      log.error("Error initializing JWT Secret Key: {}", e.getMessage(), e);
      // Throwing a specific exception might be better to halt startup if key is bad
      throw new IllegalStateException("Failed to initialize JWT key", e);
    }
  }

  /**
   * Generates a JWT token for a successfully authenticated user.
   *
   * @param authentication The Authentication object from Spring Security context.
   * @return A JWT token string.
   */
  public String generateToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
    String username = userPrincipal.getUsername();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    log.debug("Generating JWT for user: {}", username);

    // *** CHANGED HERE: Use HS256 instead of HS512 ***
    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key, Jwts.SIG.HS256) // Use HS256 algorithm
        // .claim("roles", userPrincipal.getAuthorities()) // Optionally add claims
        .compact();
  }

  /**
   * Extracts the username (subject) from a given JWT token.
   *
   * @param token The JWT token string.
   * @return The username contained within the token.
   */
  public String getUsernameFromToken(String token) {
    // No change needed here for algorithm change, verifyWith handles it
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getSubject();
  }

  /**
   * Validates the integrity and expiration of a given JWT token.
   *
   * @param authToken The JWT token string to validate.
   * @return true if the token is valid, false otherwise.
   */
  public boolean validateToken(String authToken) {
    if (key == null) {
      log.error("JWT validation failed: Secret Key is not initialized.");
      return false;
    }
    try {
      // No change needed here for algorithm change, verifyWith handles it
      Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
      log.trace("JWT token validation successful.");
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
