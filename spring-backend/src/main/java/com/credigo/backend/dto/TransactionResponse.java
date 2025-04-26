package com.credigo.backend.dto;

import com.credigo.backend.entity.TransactionStatus; // Import the enum
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing the details of a completed or attempted transaction.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

  private Integer transactionId;
  private Integer userId;
  private String username;
  private Integer productId;
  private String productName;
  private Integer quantity;
  private BigDecimal purchasePrice;
  private BigDecimal totalAmount;
  private String gameAccountId;
  private String gameServerId;
  private TransactionStatus status; // Use the enum
  private String statusMessage; // e.g., "Purchase successful", "Processing", "Insufficient funds"
  private LocalDateTime transactionTimestamp;

  // Optional: Include external API status if needed
  // private ExternalApiStatus externalApiStatus;
}
