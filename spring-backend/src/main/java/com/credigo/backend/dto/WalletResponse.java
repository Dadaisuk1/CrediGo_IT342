package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO for representing a User's Wallet information sent to the client.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

  private Integer walletId; // The ID of the wallet itself
  private Integer userId; // The ID of the user this wallet belongs to
  private String username; // The username of the user (for display)
  private BigDecimal balance; // The current wallet balance
  private LocalDateTime lastUpdatedAt; // When the balance was last modified

}
