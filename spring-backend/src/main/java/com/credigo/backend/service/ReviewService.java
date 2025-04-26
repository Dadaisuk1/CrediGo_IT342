package com.credigo.backend.service;

import com.credigo.backend.dto.ReviewRequest;
import com.credigo.backend.dto.ReviewResponse;
import java.util.List;

/**
 * Service interface for managing Product Reviews.
 */
public interface ReviewService {

  /**
   * Adds a review for a specific product by the given user.
   * Ensures the user has previously purchased the product.
   *
   * @param productId     The ID of the product being reviewed.
   * @param username      The username of the user submitting the review.
   * @param reviewRequest DTO containing the rating and comment.
   * @return ReviewResponse DTO of the newly created review.
   * @throws RuntimeException if user or product not found, user hasn't purchased
   *                          the product,
   *                          or user has already reviewed the product.
   */
  ReviewResponse addReview(Integer productId, String username, ReviewRequest reviewRequest);

  /**
   * Retrieves all reviews for a specific product.
   *
   * @param productId The ID of the product whose reviews are to be retrieved.
   * @return A list of ReviewResponse DTOs for the product.
   * @throws RuntimeException if the product is not found.
   */
  List<ReviewResponse> getReviewsForProduct(Integer productId);

  /**
   * Retrieves all reviews written by a specific user.
   *
   * @param username The username of the user whose reviews are to be retrieved.
   * @return A list of ReviewResponse DTOs written by the user.
   * @throws RuntimeException if the user is not found.
   */
  List<ReviewResponse> getReviewsByUser(String username);

  /**
   * Deletes a specific review written by the user for a product.
   *
   * @param productId The ID of the product reviewed.
   * @param username  The username of the user who wrote the review.
   * @throws RuntimeException if the user or the review is not found.
   */
  void deleteReview(Integer productId, String username);

}
