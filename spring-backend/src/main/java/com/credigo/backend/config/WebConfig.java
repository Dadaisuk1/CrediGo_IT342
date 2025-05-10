package com.credigo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Web MVC settings, including CORS (Cross-Origin Resource Sharing).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:5173",  // Vite dev server
                "https://credi-go-it-342.vercel.app"  // Production frontend
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Cache-Control", "Content-Type", "X-Frontend-Url")
            .allowCredentials(true);
    }

    /**
     * Forward requests to the single page application entry point.
     * This ensures that all routes not handled by the backend API are forwarded to index.html.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward all admin/* paths to index.html so React Router can handle them
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");

        // Also forward other frontend routes
        registry.addViewController("/home/**").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/products/**").setViewName("forward:/index.html");
        registry.addViewController("/wallet").setViewName("forward:/index.html");
        registry.addViewController("/history").setViewName("forward:/index.html");
        registry.addViewController("/wishlist").setViewName("forward:/index.html");
        registry.addViewController("/games/**").setViewName("forward:/index.html");
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
