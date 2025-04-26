package com.credigo.backend.service;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse; // We'll need a DTO for the response

/**
 * Service interface for handling purchase transactions.
 */
public interface TransactionService {

  /**
   * Processes a user's request to purchase a product (game top-up).
   *
   * @param purchaseRequest DTO containing purchase details (productId,
   *                        gameAccountId, etc.).
   * @param username        The username of the user making the purchase (obtained
   *                        from security context).
   * @return TransactionResponse DTO representing the outcome of the purchase
   *         attempt.
   * @throws RuntimeException if validation fails (product not found, insufficient
   *                          funds, etc.).
   */
  TransactionResponse processPurchase(PurchaseRequest purchaseRequest, String username);

  // Add methods later for fetching transaction history, etc.
  // List<TransactionResponse> getTransactionHistory(String username);

}
