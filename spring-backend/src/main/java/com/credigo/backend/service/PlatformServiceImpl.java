package com.credigo.backend.service;

import com.credigo.backend.dto.PlatformRequest;
import com.credigo.backend.dto.PlatformResponse;
import com.credigo.backend.entity.Platform;
import com.credigo.backend.repository.PlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the PlatformService interface.
 */
@Service // Marks this as a Spring service component
public class PlatformServiceImpl implements PlatformService {

  private static final Logger log = LoggerFactory.getLogger(PlatformServiceImpl.class);

  private final PlatformRepository platformRepository;

  // Constructor Injection
  @Autowired
  public PlatformServiceImpl(PlatformRepository platformRepository) {
    this.platformRepository = platformRepository;
  }

  @Override
  @Transactional // Ensure operation is transactional
  public PlatformResponse createPlatform(PlatformRequest platformRequest) {
    log.info("Attempting to create platform with name: {}", platformRequest.getName());

    // Check if platform name already exists
    if (platformRepository.findByName(platformRequest.getName()).isPresent()) {
      log.warn("Platform creation failed: Name '{}' already exists.", platformRequest.getName());
      throw new RuntimeException("Platform creation failed: Name '" + platformRequest.getName() + "' already exists.");
    }

    // Map DTO to Entity
    Platform platform = mapToEntity(platformRequest);

    // Save the new platform
    Platform savedPlatform = platformRepository.save(platform);
    log.info("Successfully created platform with ID: {}", savedPlatform.getId());

    // Map saved Entity back to Response DTO
    return mapToResponseDto(savedPlatform);
  }

  @Override
  @Transactional(readOnly = true) // Read-only transaction for fetching data
  public List<PlatformResponse> getAllPlatforms() {
    log.debug("Fetching all platforms");
    List<Platform> platforms = platformRepository.findAll();
    // Map list of entities to list of DTOs
    return platforms.stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PlatformResponse getPlatformById(Integer id) {
    log.debug("Fetching platform with ID: {}", id);
    Platform platform = platformRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Platform not found with ID: {}", id);
          return new RuntimeException("Platform not found with ID: " + id);
        });
    return mapToResponseDto(platform);
  }

  @Override
  @Transactional
  public PlatformResponse updatePlatform(Integer id, PlatformRequest platformRequest) {
    log.info("Attempting to update platform with ID: {}", id);

    // 1. Find the existing platform
    Platform existingPlatform = platformRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Platform update failed: Platform not found with ID: {}", id);
          return new RuntimeException("Platform update failed: Platform not found with ID: " + id);
        });

    // 2. Check if the new name conflicts with another existing platform
    Optional<Platform> conflictingPlatform = platformRepository.findByName(platformRequest.getName());
    if (conflictingPlatform.isPresent() && !conflictingPlatform.get().getId().equals(id)) {
      log.warn("Platform update failed: Name '{}' already exists for platform ID {}.", platformRequest.getName(),
          conflictingPlatform.get().getId());
      throw new RuntimeException("Platform update failed: Name '" + platformRequest.getName() + "' already exists.");
    }

    // 3. Update the fields from the request DTO
    existingPlatform.setName(platformRequest.getName());
    existingPlatform.setDescription(platformRequest.getDescription());
    existingPlatform.setLogoUrl(platformRequest.getLogoUrl());
    // Note: createdAt is not updated

    // 4. Save the updated platform
    Platform updatedPlatform = platformRepository.save(existingPlatform);
    log.info("Successfully updated platform with ID: {}", updatedPlatform.getId());

    // 5. Map to Response DTO and return
    return mapToResponseDto(updatedPlatform);
  }

  @Override
  @Transactional
  public void deletePlatform(Integer id) {
    log.info("Attempting to delete platform with ID: {}", id);

    // 1. Check if the platform exists
    if (!platformRepository.existsById(id)) {
      log.warn("Platform deletion failed: Platform not found with ID: {}", id);
      throw new RuntimeException("Platform deletion failed: Platform not found with ID: " + id);
    }

    // 2. Optional: Add check here if platform has associated products
    // Platform platform = platformRepository.findById(id).get(); // Fetch if needed
    // for check
    // if (platform.getProducts() != null && !platform.getProducts().isEmpty()) {
    // log.warn("Platform deletion failed: Platform ID {} has associated products.",
    // id);
    // throw new RuntimeException("Cannot delete platform: It has associated
    // products.");
    // }

    // 3. Delete the platform
    try {
      platformRepository.deleteById(id);
      log.info("Successfully deleted platform with ID: {}", id);
    } catch (Exception e) {
      // Catch potential constraint violation errors if products exist and FK is
      // RESTRICT
      log.error("Error deleting platform ID {}: {}", id, e.getMessage());
      throw new RuntimeException("Could not delete platform with ID: " + id + ". It might have associated products.",
          e);
    }
  }

  // --- Helper Mapping Methods ---

  private Platform mapToEntity(PlatformRequest dto) {
    Platform platform = new Platform();
    platform.setName(dto.getName());
    platform.setDescription(dto.getDescription());
    platform.setLogoUrl(dto.getLogoUrl());
    // createdAt is set automatically by @PrePersist in the entity
    return platform;
  }

  private PlatformResponse mapToResponseDto(Platform entity) {
    PlatformResponse dto = new PlatformResponse();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setLogoUrl(entity.getLogoUrl());
    dto.setCreatedAt(entity.getCreatedAt());
    return dto;
  }
}
