package com.credigo.backend.service;

import com.credigo.backend.dto.ProductRequest;
import com.credigo.backend.dto.ProductResponse;
import com.credigo.backend.entity.Platform;
import com.credigo.backend.entity.Product;
import com.credigo.backend.repository.PlatformRepository;
import com.credigo.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ProductService interface.
 */
@Service // Marks this as a Spring service component
public class ProductServiceImpl implements ProductService {

  private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ProductRepository productRepository;
  private final PlatformRepository platformRepository; // Inject PlatformRepository to find the platform

  // Constructor Injection
  @Autowired
  public ProductServiceImpl(ProductRepository productRepository, PlatformRepository platformRepository) {
    this.productRepository = productRepository;
    this.platformRepository = platformRepository;
  }

  @Override
  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest) {
    log.info("Attempting to create product: {}", productRequest.getName());

    // 1. Find the associated Platform (Game)
    Platform platform = platformRepository.findById(productRequest.getPlatformId())
        .orElseThrow(() -> {
          log.warn("Product creation failed: Platform not found with ID: {}", productRequest.getPlatformId());
          return new RuntimeException(
              "Product creation failed: Platform not found with ID: " + productRequest.getPlatformId());
        });

    // 2. Map DTO to Entity and set the platform
    Product product = mapToEntity(productRequest, platform);

    // 3. Save the new product
    Product savedProduct = productRepository.save(product);
    log.info("Successfully created product with ID: {}", savedProduct.getId());

    // 4. Map saved Entity back to Response DTO
    return mapToResponseDto(savedProduct);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductResponse> getAllAvailableProducts(Integer platformId) {
    List<Product> products;
    if (platformId != null) {
      log.debug("Fetching available products for platform ID: {}", platformId);
      // Use the custom repository method to find by platform and availability
      products = productRepository.findByPlatform_IdAndIsAvailableTrue(platformId);
    } else {
      log.debug("Fetching all available products");
      // Use the custom repository method to find all available
      products = productRepository.findByIsAvailableTrue();
      // Or use findAll and filter:
      // products =
      // productRepository.findAll().stream().filter(Product::isAvailable).collect(Collectors.toList());
    }
    // Map list of entities to list of DTOs
    return products.stream()
        .map(this::mapToResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getProductById(Integer id) {
    log.debug("Fetching product with ID: {}", id);
    Product product = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product not found with ID: {}", id);
          return new RuntimeException("Product not found with ID: " + id);
        });
    return mapToResponseDto(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(Integer id, ProductRequest productRequest) {
    log.info("Attempting to update product with ID: {}", id);

    // 1. Find the existing product
    Product existingProduct = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product update failed: Product not found with ID: {}", id);
          return new RuntimeException("Product update failed: Product not found with ID: " + id);
        });

    // 2. Find the associated Platform (check if it changed and exists)
    Platform platform = platformRepository.findById(productRequest.getPlatformId())
        .orElseThrow(() -> {
          log.warn("Product update failed: Platform not found with ID: {}", productRequest.getPlatformId());
          return new RuntimeException(
              "Product update failed: Platform not found with ID: " + productRequest.getPlatformId());
        });

    // 3. Update the fields from the request DTO
    existingProduct.setPlatform(platform); // Update platform association
    existingProduct.setName(productRequest.getName());
    existingProduct.setDescription(productRequest.getDescription());
    existingProduct.setPrice(productRequest.getPrice());
    existingProduct.setItemCode(productRequest.getItemCode());
    existingProduct.setImageUrl(productRequest.getImageUrl());
    existingProduct.setAvailable(productRequest.getIsAvailable()); // Update availability
    // Note: createdAt is not updated

    // 4. Save the updated product
    Product updatedProduct = productRepository.save(existingProduct);
    log.info("Successfully updated product with ID: {}", updatedProduct.getId());

    // 5. Map to Response DTO and return
    return mapToResponseDto(updatedProduct);
  }

  @Override
  @Transactional
  public void deleteProduct(Integer id) {
    log.info("Attempting to delete product with ID: {}", id);

    // 1. Check if the product exists
    if (!productRepository.existsById(id)) {
      log.warn("Product deletion failed: Product not found with ID: {}", id);
      throw new RuntimeException("Product deletion failed: Product not found with ID: " + id);
    }

    // 2. Delete the product
    // Note: Consider implications if transactions reference this product.
    // Depending on FK constraints, this might fail or cascade.
    // You might want to "soft delete" (set isAvailable=false) instead.
    try {
      productRepository.deleteById(id);
      log.info("Successfully deleted product with ID: {}", id);
    } catch (Exception e) {
      log.error("Error deleting product ID {}: {}", id, e.getMessage());
      throw new RuntimeException("Could not delete product with ID: " + id, e);
    }
  }

  // --- Helper Mapping Methods ---

  private Product mapToEntity(ProductRequest dto, Platform platform) {
    Product product = new Product();
    product.setPlatform(platform); // Set the associated platform
    product.setName(dto.getName());
    product.setDescription(dto.getDescription());
    product.setPrice(dto.getPrice());
    product.setItemCode(dto.getItemCode());
    product.setImageUrl(dto.getImageUrl());
    product.setAvailable(dto.getIsAvailable());
    // createdAt is set automatically by @PrePersist in the entity
    return product;
  }

  private ProductResponse mapToResponseDto(Product entity) {
    ProductResponse dto = new ProductResponse();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setPrice(entity.getPrice());
    dto.setItemCode(entity.getItemCode());
    dto.setImageUrl(entity.getImageUrl());
    dto.setAvailable(entity.isAvailable());
    dto.setCreatedAt(entity.getCreatedAt());
    // Include platform info in the response DTO
    if (entity.getPlatform() != null) {
      dto.setPlatformId(entity.getPlatform().getId());
      dto.setPlatformName(entity.getPlatform().getName());
    }
    return dto;
  }
}
