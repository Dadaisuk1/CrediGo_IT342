package com.credigo.backend;

import com.credigo.backend.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CredigoBackendApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CredigoBackendApplication.class);
        app.addInitializers(new DotenvLoader());
        app.run(args);
    }
}