package com.credigo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // Import
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // --- Add AuthenticationManager Bean ---
  /**
   * Exposes the AuthenticationManager bean from the security configuration.
   * This manager is used to process authentication requests.
   *
   * @param authenticationConfiguration Standard Spring Boot authentication
   *                                    configuration.
   * @return The AuthenticationManager bean.
   * @throws Exception If configuration fails.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
  // --- End AuthenticationManager Bean ---

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz
            // Allow unauthenticated access to registration AND login endpoints
            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
            .anyRequest().authenticated())
        .httpBasic(withDefaults()); // Still using placeholder auth method

    return http.build();
  }
}
