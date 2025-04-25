package com.credigo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a Product (Game Top-up Item).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

  @NotNull(message = "Platform ID cannot be null")
  private Integer platformId; // ID of the Platform (Game) this product belongs to

  @NotBlank(message = "Product name cannot be blank")
  @Size(max = 150, message = "Product name cannot exceed 150 characters")
  private String name; // e.g., "100 Diamonds", "Weekly Pass"

  // Optional description
  private String description;

  @NotNull(message = "Price cannot be null")
  @PositiveOrZero(message = "Price must be zero or positive")
  private BigDecimal price; // Price in CrediGo currency

  // Optional code used for external API integration
  @Size(max = 100, message = "Item code cannot exceed 100 characters")
  private String itemCode;

  // Optional image URL
  @Size(max = 255, message = "Image URL cannot exceed 255 characters")
  private String imageUrl;

  // Default availability can be set in service, but allow setting it via request
  // too
  private Boolean isAvailable = true; // Default to true if not provided

}
