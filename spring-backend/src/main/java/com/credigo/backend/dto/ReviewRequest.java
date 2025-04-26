package com.credigo.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for submitting a new Review for a Product.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

  // Note: productId will likely be a path variable in the controller,
  // but including it here can be useful for some API designs.
  // Alternatively, remove it if productId is always obtained from the path.
  // @NotNull(message = "Product ID cannot be null")
  // private Integer productId;

  @NotNull(message = "Rating cannot be null")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private Integer rating; // e.g., 1 to 5 stars

  // Optional comment
  @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
  private String comment;

}
