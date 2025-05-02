package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "platform_id", nullable = false)
  private Platform platform;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price; // Price in CrediGo currency

  @Column(name = "item_code", length = 100) // Optional: Code for external API
  private String itemCode;

  @Column(name = "image_url", length = 255)
  private String imageUrl;

  @Column(name = "is_available", nullable = false)
  private boolean isAvailable = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Transaction> transactions;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<WishlistItem> wishlistItems;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Review> reviews;

  // Optional Audit fields
  // @ManyToOne(fetch = FetchType.LAZY)
  // @JoinColumn(name = "created_by_admin_id")
  // private User createdBy;
  //
  // @ManyToOne(fetch = FetchType.LAZY)
  // @JoinColumn(name = "last_updated_by_admin_id")
  // private User lastUpdatedBy;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
