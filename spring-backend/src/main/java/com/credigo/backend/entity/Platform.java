package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platforms")
public class Platform {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "platform_id")
  private Integer id;

  @Column(nullable = false, unique = true, length = 100)
  private String name; // Game name

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "logo_url", length = 255)
  private String logoUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Product> products; // Game Top-up items hosted by this platform

  // Optional Audit fields (if you add corresponding FK columns in DB)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_admin_id")
  private User createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_updated_by_admin_id")
  private User lastUpdatedBy;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
