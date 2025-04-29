package com.credigo.backend.config;

import com.credigo.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer; // Import Customizer
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
import org.springframework.web.cors.CorsConfiguration; // Import CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource; // Import CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Import UrlBasedCorsConfigurationSource

import java.util.Arrays; // Import Arrays
import java.util.List; // Import List

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

  // --- Bean for CORS Configuration ---
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Specify allowed origins (your React app's URL)
    configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Allow frontend origin
    // Specify allowed methods (GET, POST, PUT, DELETE, etc.)
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
    // Specify allowed headers (e.g., Authorization, Content-Type)
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    // Allow credentials (like cookies, authorization headers)
    configuration.setAllowCredentials(true);
    // How long the results of a preflight request can be cached
    configuration.setMaxAge(3600L); // 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Apply this configuration to all paths on your server
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
  // --- End CORS Configuration Bean ---

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // --- Enable CORS using the bean defined above ---
        .cors(Customizer.withDefaults()) // Applies the corsConfigurationSource bean

        // Disable CSRF (common for stateless APIs)
        .csrf(AbstractHttpConfigurer::disable)

        // Configure authorization rules
        .authorizeHttpRequests(authz -> authz
            // Public endpoints
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/platforms", "/api/platforms/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/{productId}/reviews").permitAll()
            
            // Admin endpoints
            .requestMatchers("/api/platforms/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/products/admin/**").hasRole("ADMIN")

            // Any other request must be authenticated
            .anyRequest().authenticated())

        // Configure stateless session management
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Add the custom JWT filter
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
