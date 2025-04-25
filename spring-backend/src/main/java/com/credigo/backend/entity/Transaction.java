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
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transaction_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product; // The specific top-up item

  // Optional: Link to the wallet transaction if paid via wallet
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_transaction_id", unique = true)
  private WalletTransaction walletTransaction;

  @Column(nullable = false)
  private int quantity = 1; // Usually 1 for top-ups

  @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal purchasePrice; // Price at time of purchase

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount; // quantity * purchasePrice

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TransactionStatus status = TransactionStatus.PENDING;

  @Column(name = "game_account_id", nullable = false, length = 100) // Game User ID from customer
  private String gameAccountId;

  @Column(name = "game_server_id", length = 50) // Optional: Game Server/Zone ID from customer
  private String gameServerId;

  // Optional: Status from external top-up API
  @Enumerated(EnumType.STRING)
  @Column(name = "external_api_status", length = 50)
  private ExternalApiStatus externalApiStatus;

  @Column(name = "transaction_timestamp", nullable = false, updatable = false)
  private LocalDateTime transactionTimestamp;

  @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY) // Transaction can have reviews
  private Set<Review> reviews;

  @PrePersist
  protected void onCreate() {
    transactionTimestamp = LocalDateTime.now();
    if (product != null && purchasePrice == null) {
      purchasePrice = product.getPrice(); // Capture price at transaction time
    }
    if (purchasePrice != null) {
      totalAmount = purchasePrice.multiply(BigDecimal.valueOf(quantity));
    }
  }
}
