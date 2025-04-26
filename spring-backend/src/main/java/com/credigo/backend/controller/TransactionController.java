package com.credigo.backend.controller;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse;
import com.credigo.backend.entity.TransactionStatus;
import com.credigo.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for handling purchase transactions.
 */
@RestController
@RequestMapping("/api/transactions") // Base path for transaction-related endpoints
public class TransactionController {

  private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

  private final TransactionService transactionService;

  // Constructor Injection
  @Autowired
  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Endpoint for an authenticated user to initiate a product purchase.
   *
   * @param purchaseRequest DTO containing the details of the purchase.
   * @return ResponseEntity containing TransactionResponse DTO on success, or an
   *         error status.
   */
  @PostMapping("/purchase") // e.g., POST /api/transactions/purchase
  public ResponseEntity<?> initiatePurchase(@Valid @RequestBody PurchaseRequest purchaseRequest) {

    // 1. Get the currently authenticated user's username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to make purchase by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received purchase request from user: {} for product ID: {}", currentUsername,
        purchaseRequest.getProductId());

    try {
      // 2. Call the service layer to process the purchase
      TransactionResponse transactionResponse = transactionService.processPurchase(purchaseRequest, currentUsername);

      // 3. Return the transaction details
      // Determine appropriate status based on transaction outcome
      HttpStatus status = switch (transactionResponse.getStatus()) {
        // These enum constants require the import above
        case COMPLETED -> HttpStatus.CREATED;
        case PROCESSING -> HttpStatus.ACCEPTED;
        case FAILED -> HttpStatus.BAD_REQUEST;
        // Add cases for PENDING, REFUNDED if needed
        default -> HttpStatus.OK;
      };

      log.info("Purchase processed for user: {} with status: {}", currentUsername, transactionResponse.getStatus());
      return new ResponseEntity<>(transactionResponse, status);

    } catch (RuntimeException e) {
      // Catch exceptions thrown by the service
      log.error("Purchase processing failed for user {}: {}", currentUsername, e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } else if (e.getMessage().contains("Insufficient funds")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
      } else if (e.getMessage().contains("unavailable")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      // Catch any other unexpected errors
      log.error("Unexpected error during purchase for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred during purchase.");
    }
  }

  // --- TODO: Add endpoint for fetching transaction history ---
  // @GetMapping("/history")
  // public ResponseEntity<List<TransactionResponse>> getMyTransactionHistory() {
  // ... }

}
