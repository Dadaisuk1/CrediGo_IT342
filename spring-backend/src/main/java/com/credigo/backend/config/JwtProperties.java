package com.credigo.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt") // Bind properties starting with "app.jwt"
public class JwtProperties {
    private String secret; // Matches app.jwt.secret
    private long expirationMs; // Matches app.jwt.expiration-ms (uses relaxed binding)
}

