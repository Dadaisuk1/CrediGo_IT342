package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for representing Platform (Game) data sent to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformResponse {

  private Integer id; // platform_id
  private String name;
  private String description;
  private String logoUrl;
  private LocalDateTime createdAt;

  // We might add a list of associated ProductResponse DTOs here later
  // if needed for specific endpoints.
}
