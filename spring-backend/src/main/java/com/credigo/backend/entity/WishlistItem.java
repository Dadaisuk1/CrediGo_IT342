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
@Table(name = "wishlist_items")
@IdClass(WishlistItemId.class) // Use composite key class for unique user/product combo
public class WishlistItem {

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

  @Column(name = "added_at", nullable = false, updatable = false)
  private LocalDateTime addedAt;

  @PrePersist
  protected void onCreate() {
    addedAt = LocalDateTime.now();
  }
}
