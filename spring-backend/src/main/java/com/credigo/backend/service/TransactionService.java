package com.credigo.backend.service;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse;
import java.util.List; // Import List

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

  /**
   * Retrieves the transaction history for the specified user.
   *
   * @param username The username of the user whose history is to be retrieved.
   * @return A list of TransactionResponse DTOs, ordered by most recent first.
   * @throws RuntimeException if the user is not found.
   */
  List<TransactionResponse> getTransactionHistory(String username); // Add this method

}
