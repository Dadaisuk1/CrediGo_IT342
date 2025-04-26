package com.credigo.backend.controller;

import com.credigo.backend.dto.WishlistItemResponse;
import com.credigo.backend.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Import necessary annotations

import java.util.List;

/**
 * REST Controller for managing the user's wishlist.
 */
@RestController
@RequestMapping("/api/wishlist") // Base path for wishlist endpoints
public class WishlistController {

  private static final Logger log = LoggerFactory.getLogger(WishlistController.class);

  private final WishlistService wishlistService;

  // Constructor Injection
  @Autowired
  public WishlistController(WishlistService wishlistService) {
    this.wishlistService = wishlistService;
  }

  /**
   * Endpoint to get the current authenticated user's wishlist.
   *
   * @return ResponseEntity containing a list of WishlistItemResponse DTOs or an
   *         error.
   */
  @GetMapping
  public ResponseEntity<?> getCurrentUserWishlist() {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to access wishlist by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.debug("Received request for wishlist for user: {}", currentUsername);

    try {
      List<WishlistItemResponse> wishlist = wishlistService.getWishlist(currentUsername);
      return ResponseEntity.ok(wishlist); // 200 OK with the list
    } catch (Exception e) {
      log.error("Error fetching wishlist for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving wishlist.");
    }
  }

  /**
   * Endpoint to add a product to the current authenticated user's wishlist.
   *
   * @param productId The ID of the product to add (from path variable).
   * @return ResponseEntity containing the added WishlistItemResponse DTO or an
   *         error.
   */
  @PostMapping("/{productId}")
  public ResponseEntity<?> addProductToWishlist(@PathVariable Integer productId) {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to add to wishlist by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received request to add product ID: {} to wishlist for user: {}", productId, currentUsername);

    try {
      WishlistItemResponse addedItem = wishlistService.addToWishlist(currentUsername, productId);
      return new ResponseEntity<>(addedItem, HttpStatus.CREATED); // 201 Created
    } catch (RuntimeException e) {
      log.error("Failed to add product ID {} to wishlist for user {}: {}", productId, currentUsername, e.getMessage());
      // Distinguish error types if needed (e.g., ProductNotFound vs AlreadyExists)
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } else if (e.getMessage().contains("already exists")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error adding product ID {} to wishlist for user {}: {}", productId, currentUsername,
          e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding item to wishlist.");
    }
  }

  /**
   * Endpoint to remove a product from the current authenticated user's wishlist.
   *
   * @param productId The ID of the product to remove (from path variable).
   * @return ResponseEntity indicating success (204 No Content) or an error.
   */
  @DeleteMapping("/{productId}")
  public ResponseEntity<?> removeProductFromWishlist(@PathVariable Integer productId) {
    // Get authenticated username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to remove from wishlist by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received request to remove product ID: {} from wishlist for user: {}", productId, currentUsername);

    try {
      wishlistService.removeFromWishlist(currentUsername, productId);
      return ResponseEntity.noContent().build(); // 204 No Content
    } catch (RuntimeException e) {
      log.error("Failed to remove product ID {} from wishlist for user {}: {}", productId, currentUsername,
          e.getMessage());
      // Distinguish error types if needed (e.g., ItemNotFound)
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error removing product ID {} from wishlist for user {}: {}", productId, currentUsername,
          e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing item from wishlist.");
    }
  }
}
