package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
@IdClass(ReviewId.class) // Use composite key class for unique user/product combo
public class Review {

  // Use @Id on both parts of the composite key
  @Id
  @Column(name = "user_id")
  private Integer userId;

  @Id
  @Column(name = "product_id")
  private Integer productId;

  // Map the relationships using insertable=false, updatable=false on the FK
  // columns
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
  private Product product;

  // Optional link back to the specific transaction being reviewed
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id") // Allow null if review not tied to specific transaction
  private Transaction transaction;

  @Column(nullable = false)
  private int rating; // Constraint 1-5 enforced via validation or DB check

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "review_timestamp", nullable = false, updatable = false)
  private LocalDateTime reviewTimestamp;

  @PrePersist
  protected void onCreate() {
    reviewTimestamp = LocalDateTime.now();
  }
}
