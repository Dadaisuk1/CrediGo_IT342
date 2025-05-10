package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import java.math.BigDecimal;

/**
 * Service interface for managing User Wallets.
 */
public interface WalletService {

  WalletResponse getWalletByUsername(String username);

  /**
   * Retrieves the wallet details for the specified user.
   *
   * @param username The username of the user whose wallet is to be retrieved.
   * @return WalletResponse DTO containing the wallet details.
   * @throws RuntimeException if the user or their wallet is not found.
   */
  void addFundsToWallet(String username, BigDecimal amount, String PaymentIntentId, String description);

  /**
   * Adds funds to a user's wallet and creates a transaction record.
   * Checks for duplicate processing using paymentIntentId.
   *
   * @param username The username of the wallet owner
   * @param amount The amount to add (must be positive)
   * @param paymentIntentId Payment intent ID for idempotency check
   * @param description Transaction description to record
   */
  void addFundsToWallet(String username, double amount, String paymentIntentId);

  // Add other wallet-related methods later if needed (e.g., addFunds, deducted
  // funds)
}
