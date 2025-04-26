package com.credigo.backend.controller;

import com.credigo.backend.dto.ReviewRequest;
import com.credigo.backend.dto.ReviewResponse;
import com.credigo.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// Import PreAuthorize if using method-level security later
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Product Reviews.
 */
@RestController
@RequestMapping("/api") // Base path, specific paths defined in methods
public class ReviewController {

  private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

  private final ReviewService reviewService;

  // Constructor Injection
  @Autowired
  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  /**
   * Endpoint for an authenticated user to add a review for a specific product.
   *
   * @param productId     The ID of the product being reviewed (from path).
   * @param reviewRequest DTO containing rating and comment.
   * @return ResponseEntity containing the created ReviewResponse DTO or an error.
   */
  @PostMapping("/products/{productId}/reviews")
  // @PreAuthorize("hasRole('USER')") // Example: Ensure only regular users can
  // review
  public ResponseEntity<?> addReviewForProduct(@PathVariable Integer productId,
      @Valid @RequestBody ReviewRequest reviewRequest) {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to add review by unauthenticated user for product ID: {}", productId);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received request to add review for product ID: {} by user: {}", productId, currentUsername);

    try {
      ReviewResponse createdReview = reviewService.addReview(productId, currentUsername, reviewRequest);
      return new ResponseEntity<>(createdReview, HttpStatus.CREATED); // 201 Created
    } catch (RuntimeException e) {
      log.error("Failed to add review for product ID {} by user {}: {}", productId, currentUsername, e.getMessage());
      // Distinguish error types
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } else if (e.getMessage().contains("already reviewed")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
      } else if (e.getMessage().contains("purchased")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 Forbidden
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error adding review for product ID {} by user {}: {}", productId, currentUsername,
          e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding review.");
    }
  }

  /**
   * Endpoint to get all reviews for a specific product. (Publicly accessible)
   *
   * @param productId The ID of the product.
   * @return ResponseEntity containing a list of ReviewResponse DTOs or an error.
   */
  @GetMapping("/products/{productId}/reviews")
  public ResponseEntity<?> getReviewsForProduct(@PathVariable Integer productId) {
    log.debug("Received request to get reviews for product ID: {}", productId);
    try {
      List<ReviewResponse> reviews = reviewService.getReviewsForProduct(productId);
      return ResponseEntity.ok(reviews); // 200 OK
    } catch (RuntimeException e) {
      log.warn("Failed to get reviews for product ID {}: {}", productId, e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error getting reviews for product ID {}: {}", productId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving reviews.");
    }
  }

  /**
   * Endpoint for an authenticated user to get all reviews they have written.
   *
   * @return ResponseEntity containing a list of ReviewResponse DTOs or an error.
   */
  @GetMapping("/reviews/user") // Example path: /api/reviews/user
  public ResponseEntity<?> getMyReviews() {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to access user reviews by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.debug("Received request for reviews written by user: {}", currentUsername);

    try {
      List<ReviewResponse> reviews = reviewService.getReviewsByUser(currentUsername);
      return ResponseEntity.ok(reviews); // 200 OK
    } catch (Exception e) {
      log.error("Error fetching reviews for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving your reviews.");
    }
  }

  /**
   * Endpoint for an authenticated user to delete their own review for a specific
   * product.
   *
   * @param productId The ID of the product whose review is to be deleted.
   * @return ResponseEntity indicating success (204 No Content) or an error.
   */
  @DeleteMapping("/products/{productId}/reviews")
  // @PreAuthorize("hasRole('USER')") // Or check ownership in service
  public ResponseEntity<?> deleteMyReviewForProduct(@PathVariable Integer productId) {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to delete review by unauthenticated user for product ID: {}", productId);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received request to delete review for product ID: {} by user: {}", productId, currentUsername);

    try {
      reviewService.deleteReview(productId, currentUsername);
      return ResponseEntity.noContent().build(); // 204 No Content
    } catch (RuntimeException e) {
      log.error("Failed to delete review for product ID {} by user {}: {}", productId, currentUsername, e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error deleting review for product ID {} by user {}: {}", productId, currentUsername,
          e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting review.");
    }
  }

}
