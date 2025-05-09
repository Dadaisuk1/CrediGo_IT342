package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.entity.Wallet;
import java.math.BigDecimal;
import java.util.Map;

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
   * Get wallet by ID
   * 
   * @param walletId The wallet ID
   * @return The wallet if found, null otherwise
   */
  Wallet getWalletById(Long walletId);
  
  /**
   * Update an existing wallet
   * 
   * @param wallet The wallet to update
   * @return The updated wallet
   */
  Wallet updateWallet(Wallet wallet);
  
  /**
   * Process a product purchase by deducting from the wallet balance and creating a transaction
   * 
   * @param username The username of the buyer
   * @param productId The ID of the purchased product
   * @param productName The name of the purchased product
   * @param price The price of the product
   * @param description Additional description of the purchase (optional)
   * @return A map containing transaction details and updated wallet info
   * @throws IllegalArgumentException if the wallet has insufficient balance
   * @throws RuntimeException if the wallet is not found or other errors occur
   */
  Map<String, Object> processPurchase(String username, Long productId, String productName, 
      BigDecimal price, String description);

  // Add other wallet-related methods later if needed (e.g., addFunds, deducted
  // funds)
}
