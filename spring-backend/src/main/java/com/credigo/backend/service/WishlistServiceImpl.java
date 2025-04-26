package com.credigo.backend.service;

import com.credigo.backend.dto.WishlistItemResponse;
import com.credigo.backend.entity.Platform; // Import Platform
import com.credigo.backend.entity.Product;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.WishlistItem;
import com.credigo.backend.entity.WishlistItemId; // Import WishlistItemId
import com.credigo.backend.repository.ProductRepository;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.repository.WishlistItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the WishlistService interface.
 */
@Service // Marks this as a Spring service component
public class WishlistServiceImpl implements WishlistService {

  private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

  private final WishlistItemRepository wishlistItemRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  // Constructor Injection
  @Autowired
  public WishlistServiceImpl(WishlistItemRepository wishlistItemRepository,
      UserRepository userRepository,
      ProductRepository productRepository) {
    this.wishlistItemRepository = wishlistItemRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true) // Read-only operation
  public List<WishlistItemResponse> getWishlist(String username) {
    log.debug("Fetching wishlist for user: {}", username);

    // 1. Find the user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Fetch wishlist items for the user
    List<WishlistItem> items = wishlistItemRepository.findByUserId(user.getId());

    // 3. Map entities to response DTOs
    List<WishlistItemResponse> responseList = items.stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());

    log.debug("Found {} items in wishlist for user: {}", responseList.size(), username);
    return responseList;
  }

  @Override
  @Transactional
  public WishlistItemResponse addToWishlist(String username, Integer productId) {
    log.info("Attempting to add product ID: {} to wishlist for user: {}", productId, username);

    // 1. Find the user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Find the product
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

    // 3. Create the composite ID for checking existence
    WishlistItemId wishlistItemId = new WishlistItemId(user.getId(), productId);

    // 4. Check if item already exists in wishlist
    if (wishlistItemRepository.existsById(wishlistItemId)) {
      log.warn("Product ID: {} already exists in wishlist for user: {}", productId, username);
      throw new RuntimeException("Product already exists in wishlist.");
    }

    // 5. Create and save the new wishlist item
    WishlistItem newItem = new WishlistItem();
    newItem.setUserId(user.getId());
    newItem.setProductId(productId);
    // The @PrePersist in WishlistItem entity sets addedAt automatically
    // We need to manually set the User and Product references for the mapping
    // helper
    newItem.setUser(user);
    newItem.setProduct(product);

    WishlistItem savedItem = wishlistItemRepository.save(newItem);
    log.info("Successfully added product ID: {} to wishlist for user: {}", productId, username);

    // Map the saved item (which now includes User and Product) to the response DTO
    return mapToResponseDto(savedItem);
  }

  @Override
  @Transactional
  public void removeFromWishlist(String username, Integer productId) {
    log.info("Attempting to remove product ID: {} from wishlist for user: {}", productId, username);

    // 1. Find the user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Create the composite ID to identify the item to delete
    WishlistItemId wishlistItemId = new WishlistItemId(user.getId(), productId);

    // 3. Check if the item exists before trying to delete
    if (!wishlistItemRepository.existsById(wishlistItemId)) {
      log.warn("Product ID: {} not found in wishlist for user: {}", productId, username);
      throw new RuntimeException("Item not found in wishlist.");
    }

    // 4. Delete the item using the composite ID
    wishlistItemRepository.deleteById(wishlistItemId);
    // Alternatively, use the custom delete method if defined in the repository:
    // wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);

    log.info("Successfully removed product ID: {} from wishlist for user: {}", productId, username);
  }

  // --- Helper Mapping Method ---
  private WishlistItemResponse mapToResponseDto(WishlistItem entity) {
    if (entity == null || entity.getProduct() == null) {
      // Handle cases where entity or essential related data is missing
      log.warn("Attempted to map null WishlistItem or WishlistItem with null Product.");
      return null; // Or throw an exception, or return a default DTO
    }

    WishlistItemResponse dto = new WishlistItemResponse();
    Product product = entity.getProduct();
    Platform platform = product.getPlatform(); // Get platform from product

    dto.setProductId(product.getId());
    dto.setProductName(product.getName());
    dto.setProductDescription(product.getDescription());
    dto.setProductPrice(product.getPrice());
    dto.setProductImageUrl(product.getImageUrl());
    dto.setProductIsAvailable(product.isAvailable());
    dto.setAddedAt(entity.getAddedAt());

    if (platform != null) {
      dto.setPlatformId(platform.getId());
      dto.setPlatformName(platform.getName());
    } else {
      log.warn("Product ID {} associated with WishlistItem has a null Platform.", product.getId());
      // Set platform details to null or default values
      dto.setPlatformId(null);
      dto.setPlatformName("Unknown Platform");
    }

    return dto;
  }
}
