package com.credigo.backend.repository;

import com.credigo.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
  // Example: Find products by platform (Game)
  List<Product> findByPlatform_IdAndIsAvailableTrue(Integer platformId);

  // Example: Find available products by name containing search term
  List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);

  List<Product> findByIsAvailableTrue();
}
