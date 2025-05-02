package com.credigo.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component; // Or use @EnableConfigurationProperties

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
@Component // Make it a Spring bean OR use @EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.jwt") // Bind properties starting with "app.jwt" 
public class JwtProperties {

  private String secret; // Matches app.jwt.secret
  private long expirationMs; // Matches app.jwt.expiration-ms (uses relaxed binding)
  @Bean
    public String jwtSecret() {
        return Dotenv.configure().ignoreIfMissing().load().get("JWT_SECRET", "fallback-secret-key");
    }
}
