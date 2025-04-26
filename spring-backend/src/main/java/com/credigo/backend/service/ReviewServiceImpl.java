package com.credigo.backend.service;

import com.credigo.backend.dto.ReviewRequest;
import com.credigo.backend.dto.ReviewResponse;
import com.credigo.backend.entity.*;
import com.credigo.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.credigo.backend.repository.TransactionRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ReviewService interface.
 */
@Service // Marks this as a Spring service component
public class ReviewServiceImpl implements ReviewService {

  private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final TransactionRepository transactionRepository; // Needed to check purchase history

  // Constructor Injection
  @Autowired
  public ReviewServiceImpl(ReviewRepository reviewRepository,
      UserRepository userRepository,
      ProductRepository productRepository,
      TransactionRepository transactionRepository) {
    this.reviewRepository = reviewRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.transactionRepository = transactionRepository;
  }

  @Override
  @Transactional
  public ReviewResponse addReview(Integer productId, String username, ReviewRequest reviewRequest) {
    log.info("Attempting to add review for product ID: {} by user: {}", productId, username);

    // 1. Find User
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Find Product
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

    // 3. Check if user has purchased this product (at least one completed
    // transaction)
    boolean hasPurchased = transactionRepository.existsByUser_IdAndProduct_IdAndStatus(user.getId(), productId,
        TransactionStatus.COMPLETED);

    if (!hasPurchased) {
      log.warn("User {} attempted to review product ID {} without purchasing it.", username, productId);
      throw new RuntimeException("You can only review products you have purchased.");
    }

    // 4. Check if user has already reviewed this product
    ReviewId reviewId = new ReviewId(user.getId(), productId);
    if (reviewRepository.existsById(reviewId)) {
      log.warn("User {} has already reviewed product ID {}.", username, productId);
      throw new RuntimeException("You have already reviewed this product.");
    }

    // 5. Create and save the new review
    Review review = new Review();
    review.setUserId(user.getId()); // Set composite key parts
    review.setProductId(productId);
    review.setUser(user); // Set relationships
    review.setProduct(product);
    review.setRating(reviewRequest.getRating());
    review.setComment(reviewRequest.getComment());
    // reviewTimestamp is set automatically by @PrePersist in Review entity

    Review savedReview = reviewRepository.save(review);
    log.info("Successfully added review for product ID: {} by user: {}", productId, username);

    // 6. Map to response DTO
    return mapToResponseDto(savedReview);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewResponse> getReviewsForProduct(Integer productId) {
    log.debug("Fetching reviews for product ID: {}", productId);

    // 1. Check if product exists (optional, but good practice)
    if (!productRepository.existsById(productId)) {
      throw new RuntimeException("Product not found with ID: " + productId);
    }

    // 2. Fetch reviews using repository method
    List<Review> reviews = reviewRepository.findByProductId(productId);

    // 3. Map to DTOs
    List<ReviewResponse> responseList = reviews.stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());

    log.debug("Found {} reviews for product ID: {}", responseList.size(), productId);
    return responseList;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewResponse> getReviewsByUser(String username) {
    log.debug("Fetching reviews written by user: {}", username);

    // 1. Find user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Fetch reviews using repository method
    List<Review> reviews = reviewRepository.findByUserId(user.getId());

    // 3. Map to DTOs
    List<ReviewResponse> responseList = reviews.stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());

    log.debug("Found {} reviews written by user: {}", responseList.size(), username);
    return responseList;
  }

  @Override
  @Transactional
  public void deleteReview(Integer productId, String username) {
    log.info("Attempting to delete review for product ID: {} by user: {}", productId, username);

    // 1. Find user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Create composite ID for the review
    ReviewId reviewId = new ReviewId(user.getId(), productId);

    // 3. Check if the review exists before attempting deletion
    if (!reviewRepository.existsById(reviewId)) {
      log.warn("Review not found for product ID {} by user {}", productId, username);
      throw new RuntimeException("Review not found.");
    }

    // 4. Delete the review
    reviewRepository.deleteById(reviewId);
    log.info("Successfully deleted review for product ID: {} by user: {}", productId, username);
  }

  // --- Helper Mapping Method ---
  private ReviewResponse mapToResponseDto(Review entity) {
    if (entity == null)
      return null;

    ReviewResponse dto = new ReviewResponse();
    // Assuming Review entity has getId() or composite key fields directly
    // accessible
    // If using composite key, might not have a single 'reviewId' easily available
    // dto.setReviewId(entity.getId()); // Adjust if using composite key directly
    dto.setRating(entity.getRating());
    dto.setComment(entity.getComment());
    dto.setReviewTimestamp(entity.getReviewTimestamp());

    if (entity.getUser() != null) {
      dto.setUserId(entity.getUser().getId());
      dto.setUsername(entity.getUser().getUsername());
    }
    // Optionally include product details if needed in this context
    // if (entity.getProduct() != null) {
    // dto.setProductId(entity.getProduct().getId());
    // dto.setProductName(entity.getProduct().getName());
    // }

    return dto;
  }
}
