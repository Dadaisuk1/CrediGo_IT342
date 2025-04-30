package com.credigo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Web MVC settings, including CORS (Cross-Origin Resource Sharing).
 */
@Configuration
public class WebConfig {

    /**
     * Defines CORS configuration for the application.
     * Allows requests from the specified frontend origin for API endpoints.
     *
     * @return WebMvcConfigurer with CORS settings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
      return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
          registry.addMapping("/api/**")
              .allowedOrigins("https://credi-go.vercel.app")
              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
              .allowedHeaders("*")
              .allowCredentials(true);
        }
      };
    }
}