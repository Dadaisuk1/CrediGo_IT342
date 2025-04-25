package com.credigo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating or updating a Platform (Game).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRequest {

  @NotBlank(message = "Platform name cannot be blank")
  @Size(max = 100, message = "Platform name cannot exceed 100 characters")
  private String name; // Game Name

  // Description is optional, so no @NotBlank
  private String description;

  // Logo URL is optional, could add @URL validation if needed
  @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
  private String logoUrl;

}
