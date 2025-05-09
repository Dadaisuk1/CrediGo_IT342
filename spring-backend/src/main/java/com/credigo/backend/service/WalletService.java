package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.dto.WalletTransactionResponse;
import java.math.BigDecimal;
import java.util.List;

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
   * Records a pending transaction for wallet top-up.
   *
   * @param username The username of the user
   * @param amount The amount being processed
   * @param transactionType The type of transaction (e.g., WALLET_TOPUP)
   * @param description Description of the transaction
   * @param paymentId The payment ID from PayMongo
   * @param status The initial status of the transaction
   */
  void recordPendingTransaction(String username, BigDecimal amount, String transactionType,
                              String description, String paymentId, String status);

  /**
   * Gets all wallet transactions for a user, ordered by most recent first.
   *
   * @param username The username of the user
   * @return List of wallet transaction responses
   */
  List<WalletTransactionResponse> getWalletTransactions(String username);

  // Add other wallet-related methods later if needed (e.g., addFunds, deducted
  // funds)
}
