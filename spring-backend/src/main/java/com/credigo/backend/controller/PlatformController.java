package com.credigo.backend.controller;

import com.credigo.backend.dto.PlatformRequest;
import com.credigo.backend.dto.PlatformResponse;
import com.credigo.backend.service.PlatformService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Import PreAuthorize if using method-level security later
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Platforms (Games).
 */
@RestController
@RequestMapping("/api/platforms") // Base path for platform-related endpoints
public class PlatformController {

  private static final Logger log = LoggerFactory.getLogger(PlatformController.class);

  private final PlatformService platformService;

  // Constructor Injection
  @Autowired
  public PlatformController(PlatformService platformService) {
    this.platformService = platformService;
  }

  // --- Endpoint to Create a Platform (Admin Only) ---
  // Note: Security will be added later in SecurityConfig or using @PreAuthorize
  // @PreAuthorize("hasRole('ADMIN')") // Example of method-level security
  @PostMapping("/admin") // Differentiate admin path, e.g., /api/platforms/admin
  public ResponseEntity<?> createPlatform(@Valid @RequestBody PlatformRequest platformRequest) {
    log.info("Received request to create platform: {}", platformRequest.getName());
    try {
      PlatformResponse createdPlatform = platformService.createPlatform(platformRequest);
      return new ResponseEntity<>(createdPlatform, HttpStatus.CREATED); // 201 Created
    } catch (RuntimeException e) {
      log.error("Platform creation failed: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
    }
  }

  // --- Endpoint to Get All Platforms (Public) ---
  @GetMapping
  public ResponseEntity<List<PlatformResponse>> getAllPlatforms() {
    log.debug("Received request to get all platforms");
    List<PlatformResponse> platforms = platformService.getAllPlatforms();
    return ResponseEntity.ok(platforms); // 200 OK
  }

  // --- Endpoint to Get a Single Platform by ID (Public) ---
  @GetMapping("/{id}")
  public ResponseEntity<?> getPlatformById(@PathVariable Integer id) {
    log.debug("Received request to get platform with ID: {}", id);
    try {
      PlatformResponse platform = platformService.getPlatformById(id);
      return ResponseEntity.ok(platform); // 200 OK
    } catch (RuntimeException e) {
      log.warn("Failed to get platform with ID {}: {}", id, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
    }
  }

  // --- Endpoint to Update a Platform (Admin Only) ---
  // Note: Security will be added later
  // @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/admin/{id}") // Differentiate admin path
  public ResponseEntity<?> updatePlatform(@PathVariable Integer id,
      @Valid @RequestBody PlatformRequest platformRequest) {
    log.info("Received request to update platform ID: {}", id);
    try {
      PlatformResponse updatedPlatform = platformService.updatePlatform(id, platformRequest);
      return ResponseEntity.ok(updatedPlatform); // 200 OK
    } catch (RuntimeException e) {
      log.error("Platform update failed for ID {}: {}", id, e.getMessage());
      // Distinguish between Not Found and Bad Request (e.g., name conflict)
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
      } else {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
      }
    }
  }

  // --- Endpoint to Delete a Platform (Admin Only) ---
  // Note: Security will be added later
  // @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/admin/{id}") // Differentiate admin path
  public ResponseEntity<?> deletePlatform(@PathVariable Integer id) {
    log.info("Received request to delete platform ID: {}", id);
    try {
      platformService.deletePlatform(id);
      return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
    } catch (RuntimeException e) {
      log.error("Platform deletion failed for ID {}: {}", id, e.getMessage());
      // Distinguish between Not Found and Bad Request (e.g., constraint violation)
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
      } else {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
      }
    }
  }
}
