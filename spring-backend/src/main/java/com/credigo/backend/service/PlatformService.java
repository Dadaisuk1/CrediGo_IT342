package com.credigo.backend.service;

import com.credigo.backend.dto.PlatformRequest;
import com.credigo.backend.dto.PlatformResponse;
import java.util.List;

/**
 * Service interface for managing Platforms (Games).
 */
public interface PlatformService {

  /**
   * Creates a new Platform.
   *
   * @param platformRequest DTO containing details for the new platform.
   * @return PlatformResponse DTO of the newly created platform.
   * @throws RuntimeException if a platform with the same name already exists.
   */
  PlatformResponse createPlatform(PlatformRequest platformRequest);

  /**
   * Retrieves all Platforms.
   *
   * @return A list of PlatformResponse DTOs.
   */
  List<PlatformResponse> getAllPlatforms();

  /**
   * Retrieves a specific Platform by its ID.
   *
   * @param id The ID of the platform to retrieve.
   * @return PlatformResponse DTO of the found platform.
   * @throws RuntimeException if the platform with the given ID is not found.
   */
  PlatformResponse getPlatformById(Integer id);

  /**
   * Updates an existing Platform.
   *
   * @param id              The ID of the platform to update.
   * @param platformRequest DTO containing the updated details.
   * @return PlatformResponse DTO of the updated platform.
   * @throws RuntimeException if the platform with the given ID is not found or if
   *                          the new name conflicts with another existing
   *                          platform.
   */
  PlatformResponse updatePlatform(Integer id, PlatformRequest platformRequest);

  /**
   * Deletes a Platform by its ID.
   *
   * @param id The ID of the platform to delete.
   * @throws RuntimeException if the platform with the given ID is not found or
   *                          cannot be deleted (e.g., has associated products).
   */
  void deletePlatform(Integer id);

}
