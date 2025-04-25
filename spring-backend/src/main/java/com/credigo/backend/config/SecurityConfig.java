package com.credigo.backend.config;

import com.credigo.backend.security.jwt.JwtAuthenticationFilter; // Import the custom filter
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Import SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import this filter class

// Removed: import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // Inject the custom JWT filter
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
        // Disable CSRF protection (common for stateless REST APIs)
        .csrf(AbstractHttpConfigurer::disable)

        // Configure authorization rules
        .authorizeHttpRequests(authz -> authz
            // Allow unauthenticated access to auth endpoints
            .requestMatchers("/api/auth/**").permitAll() // Allow all under /api/auth/
            // Add other public endpoints here if needed
            // .requestMatchers("/api/products/public/**").permitAll()
            // Require authentication for all other requests
            .anyRequest().authenticated())

        // Configure session management to be STATELESS
        // Since we are using JWT, we don't need the server to manage sessions
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Remove HTTP Basic authentication (no longer needed)
    // .httpBasic(withDefaults());

    // Add the custom JWT filter BEFORE the standard
    // UsernamePasswordAuthenticationFilter
    // This ensures our token validation runs first for relevant requests
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
