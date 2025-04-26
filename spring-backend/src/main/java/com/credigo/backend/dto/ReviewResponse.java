package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for representing a Review when displaying it to users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

  // Review Details
  // Maybe include review_id if needed for editing/deleting later
  // private Integer reviewId;
  private Integer rating;
  private String comment;
  private LocalDateTime reviewTimestamp;

  // User who wrote the review
  private Integer userId;
  private String username;

  // Product being reviewed (optional, depending on context)
  // If showing reviews on a product page, productId might be redundant.
  // private Integer productId;
  // private String productName;

}
