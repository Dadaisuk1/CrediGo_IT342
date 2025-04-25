package com.credigo.backend.repository;

import com.credigo.backend.entity.Review;
import com.credigo.backend.entity.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
  List<Review> findByProductId(Integer productId);

  List<Review> findByUserId(Integer userId);
}
