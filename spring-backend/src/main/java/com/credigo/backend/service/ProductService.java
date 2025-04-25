package com.credigo.backend.service;

import com.credigo.backend.dto.ProductRequest;
import com.credigo.backend.dto.ProductResponse;
import java.util.List;

/**
 * Service interface for managing Products (Game Top-up Items).
 */
public interface ProductService {

  /**
   * Creates a new Product associated with a Platform.
   *
   * @param productRequest DTO containing details for the new product.
   * @return ProductResponse DTO of the newly created product.
   * @throws RuntimeException if the associated Platform is not found.
   */
  ProductResponse createProduct(ProductRequest productRequest);

  /**
   * Retrieves all available Products, optionally filtered by Platform ID.
   *
   * @param platformId Optional ID of the platform to filter by. If null, returns
   *                   all available products.
   * @return A list of ProductResponse DTOs for available products.
   */
  List<ProductResponse> getAllAvailableProducts(Integer platformId);

  /**
   * Retrieves a specific Product by its ID.
   *
   * @param id The ID of the product to retrieve.
   * @return ProductResponse DTO of the found product.
   * @throws RuntimeException if the product with the given ID is not found.
   */
  ProductResponse getProductById(Integer id);

  /**
   * Updates an existing Product.
   *
   * @param id             The ID of the product to update.
   * @param productRequest DTO containing the updated details.
   * @return ProductResponse DTO of the updated product.
   * @throws RuntimeException if the product or associated platform is not found.
   */
  ProductResponse updateProduct(Integer id, ProductRequest productRequest);

  /**
   * Deletes a Product by its ID.
   *
   * @param id The ID of the product to delete.
   * @throws RuntimeException if the product with the given ID is not found.
   */
  void deleteProduct(Integer id);

}
