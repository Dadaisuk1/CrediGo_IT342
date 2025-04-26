package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;

/**
 * Service interface for managing User Wallets.
 */
public interface WalletService {

  /**
   * Retrieves the wallet details for the specified user.
   *
   * @param username The username of the user whose wallet is to be retrieved.
   * @return WalletResponse DTO containing the wallet details.
   * @throws RuntimeException if the user or their wallet is not found.
   */
  WalletResponse getWalletByUsername(String username);

  // Add other wallet-related methods later if needed (e.g., addFunds, deducted
  // funds)
}
