package com.credigo.backend.config;

import com.credigo.backend.entity.Role;
import com.credigo.backend.repository.RoleRepository;
import org.slf4j.Logger; // Use SLF4J for logging
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this as a configuration class
public class DataInitializerConfig {

  private static final Logger log = LoggerFactory.getLogger(DataInitializerConfig.class);

  /**
   * Initializes default roles in the database if they don't already exist.
   * Runs once on application startup.
   *
   * @param roleRepository The repository to interact with the roles table.
   * @return A CommandLineRunner bean.
   */
  @Bean
  CommandLineRunner initRoles(RoleRepository roleRepository) {
    return args -> {
      log.info("Checking for default roles...");

      // --- Create USER Role if not exists ---
      String userRoleName = "USER";
      if (roleRepository.findByRoleName(userRoleName).isEmpty()) {
        Role userRole = new Role();
        userRole.setRoleName(userRoleName);
        roleRepository.save(userRole);
        log.info("Created default role: {}", userRoleName);
      } else {
        log.info("Role {} already exists.", userRoleName);
      }

      // --- Create ADMIN Role if not exists ---
      String adminRoleName = "ADMIN";
      if (roleRepository.findByRoleName(adminRoleName).isEmpty()) {
        Role adminRole = new Role();
        adminRole.setRoleName(adminRoleName);
        roleRepository.save(adminRole);
        log.info("Created default role: {}", adminRoleName);
      } else {
        log.info("Role {} already exists.", adminRoleName);
      }

      log.info("Default role check complete.");
    };
  }
}
