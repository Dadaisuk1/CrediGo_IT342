package com.credigo.backend.repository;

import com.credigo.backend.entity.WishlistItem;
import com.credigo.backend.entity.WishlistItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, WishlistItemId> {
  List<WishlistItem> findByUserId(Integer userId);

  // Optional: Method to delete by composite key parts directly
  void deleteByUserIdAndProductId(Integer userId, Integer productId);
}
