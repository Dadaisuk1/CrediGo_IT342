package com.credigo.backend.config;

import com.credigo.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Optional: Enable if you plan to use @PreAuthorize
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // Disable CSRF

        // Configure authorization rules
        .authorizeHttpRequests(authz -> authz
            // --- Public Endpoints ---
            .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoints
            .requestMatchers(HttpMethod.GET, "/api/platforms", "/api/platforms/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll() // Allow reading products
            .requestMatchers("/api/payments/stripe/webhook").permitAll() // *** Allow Stripe webhook ***

            // --- Admin Endpoints ---
            .requestMatchers("/api/platforms/admin/**").hasRole("ADMIN") // Require ADMIN for platform CUD
            .requestMatchers("/api/products/admin/**").hasRole("ADMIN") // Require ADMIN for product CUD
            // Add other admin endpoints here

            // --- Authenticated Endpoints (Any Role) ---
            // Examples:
            // .requestMatchers("/api/wallet/**").authenticated()
            // .requestMatchers("/api/transactions/purchase").authenticated() // Purchase
            // needs auth
            // .requestMatchers("/api/transactions/history").authenticated() // History
            // needs auth

            // --- Default ---
            // Any other request must be authenticated
            .anyRequest().authenticated())

        // Configure stateless session management
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Add the custom JWT filter before the standard username/password filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
