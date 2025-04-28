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
@Table(name = "wallets")
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_id")
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true) // Link to User
  private User user;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(name = "last_updated_at")
  private LocalDateTime lastUpdatedAt;

  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<WalletTransaction> walletTransactions;

  @PreUpdate
  @PrePersist
  protected void onUpdate() {
    lastUpdatedAt = LocalDateTime.now();
  }
}
