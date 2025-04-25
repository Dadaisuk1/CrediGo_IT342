package com.credigo.backend.controller;

import com.credigo.backend.dto.ProductRequest;
import com.credigo.backend.dto.ProductResponse;
import com.credigo.backend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Import PreAuthorize if using method-level security later
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Products (Game Top-up Items).
 */
@RestController
@RequestMapping("/api/products") // Base path for product-related endpoints
public class ProductController {

  private static final Logger log = LoggerFactory.getLogger(ProductController.class);

  private final ProductService productService;

  // Constructor Injection
  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  // --- Endpoint to Create a Product (Admin Only) ---
  // Note: Security will be added later in SecurityConfig or using @PreAuthorize
  // @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin") // Differentiate admin path
  public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
    log.info("Received request to create product: {}", productRequest.getName());
    try {
      ProductResponse createdProduct = productService.createProduct(productRequest);
      return new ResponseEntity<>(createdProduct, HttpStatus.CREATED); // 201 Created
    } catch (RuntimeException e) {
      log.error("Product creation failed: {}", e.getMessage());
      // Distinguish between Not Found (Platform) and other errors if needed
      if (e.getMessage().contains("Platform not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
      } else {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
      }
    }
  }

  // --- Endpoint to Get All Available Products (Public) ---
  // Allows optional filtering by platformId
  @GetMapping
  public ResponseEntity<List<ProductResponse>> getAllAvailableProducts(
      @RequestParam(required = false) Integer platformId) { // Optional request parameter

    if (platformId != null) {
      log.debug("Received request to get available products for platform ID: {}", platformId);
    } else {
      log.debug("Received request to get all available products");
    }
    List<ProductResponse> products = productService.getAllAvailableProducts(platformId);
    return ResponseEntity.ok(products); // 200 OK
  }

  // --- Endpoint to Get a Single Product by ID (Public) ---
  @GetMapping("/{id}")
  public ResponseEntity<?> getProductById(@PathVariable Integer id) {
    log.debug("Received request to get product with ID: {}", id);
    try {
      ProductResponse product = productService.getProductById(id);
      return ResponseEntity.ok(product); // 200 OK
    } catch (RuntimeException e) {
      log.warn("Failed to get product with ID {}: {}", id, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
    }
  }

  // --- Endpoint to Update a Product (Admin Only) ---
  // Note: Security will be added later
  // @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/admin/{id}") // Differentiate admin path
  public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductRequest productRequest) {
    log.info("Received request to update product ID: {}", id);
    try {
      ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
      return ResponseEntity.ok(updatedProduct); // 200 OK
    } catch (RuntimeException e) {
      log.error("Product update failed for ID {}: {}", id, e.getMessage());
      // Distinguish between Not Found (Product or Platform) and other errors
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
      } else {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
      }
    }
  }

  // --- Endpoint to Delete a Product (Admin Only) ---
  // Note: Security will be added later
  // @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/admin/{id}") // Differentiate admin path
  public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
    log.info("Received request to delete product ID: {}", id);
    try {
      productService.deleteProduct(id);
      return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
    } catch (RuntimeException e) {
      log.error("Product deletion failed for ID {}: {}", id, e.getMessage());
      // Distinguish between Not Found and Bad Request (e.g., constraint violation)
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
      } else {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
      }
    }
  }
}
