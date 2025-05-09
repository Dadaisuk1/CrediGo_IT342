package com.credigo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for CrediGo backend.
 * 
 * NOTE: There is a conflict between two implementations of UserService:
 * - com.credigo.backend.service.UserServiceImpl
 * - com.credigo.backend.service.impl.UserServiceImpl
 * 
 * Please delete one of these classes (preferably the one in service.impl) 
 * to resolve the bean definition conflict.
 */
@SpringBootApplication
public class CredigoBackendApplication {
<<<<<<< HEAD
    public static void main(String[] args) {
        SpringApplication.run(CredigoBackendApplication.class, args);
    }
}
=======

	public static void main(String[] args) {
		SpringApplication.run(CredigoBackendApplication.class, args);
	}

}
>>>>>>> mobile
