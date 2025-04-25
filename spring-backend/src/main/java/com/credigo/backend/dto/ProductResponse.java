package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for representing Product (Game Top-up Item) data sent to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private Integer id; // product_id
  private String name;
  private String description;
  private BigDecimal price;
  private String itemCode;
  private String imageUrl;
  private boolean isAvailable;
  private LocalDateTime createdAt;

  // Include basic info about the associated platform (Game)
  private Integer platformId;
  private String platformName;

  // You could add more platform details if needed
}
