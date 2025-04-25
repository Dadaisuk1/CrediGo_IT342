package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_transaction_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 50)
  private WalletTransactionType transactionType;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  // Optional link to the purchase transaction
  @Column(name = "related_transaction_id")
  private Integer relatedTransactionId;

  @Column(length = 255)
  private String description;

  @Column(name = "transaction_timestamp", nullable = false, updatable = false)
  private LocalDateTime transactionTimestamp;

  @PrePersist
  protected void onCreate() {
    transactionTimestamp = LocalDateTime.now();
  }
}
