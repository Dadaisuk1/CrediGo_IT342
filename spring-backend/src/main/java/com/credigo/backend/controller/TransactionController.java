package com.credigo.backend.controller;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse;
import com.credigo.backend.entity.TransactionStatus; // Ensure this import exists
import com.credigo.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Import GetMapping

import java.util.List; // Import List

/**
 * REST Controller for handling purchase transactions and history.
 */
@RestController
@RequestMapping("/api/transactions") // Base path
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
  @PostMapping("/purchase")
  public ResponseEntity<?> initiatePurchase(@Valid @RequestBody PurchaseRequest purchaseRequest) {
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
      TransactionResponse transactionResponse = transactionService.processPurchase(purchaseRequest, currentUsername);
      HttpStatus status = switch (transactionResponse.getStatus()) {
        case COMPLETED -> HttpStatus.CREATED;
        case PROCESSING -> HttpStatus.ACCEPTED;
        case FAILED -> HttpStatus.BAD_REQUEST;
        default -> HttpStatus.OK;
      };
      log.info("Purchase processed for user: {} with status: {}", currentUsername, transactionResponse.getStatus());
      return new ResponseEntity<>(transactionResponse, status);
    } catch (RuntimeException e) {
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
      log.error("Unexpected error during purchase for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred during purchase.");
    }
  }

  // --- New Endpoint for fetching transaction history ---
  /**
   * Endpoint for an authenticated user to retrieve their own transaction history.
   *
   * @return ResponseEntity containing a list of TransactionResponse DTOs.
   */
  @GetMapping("/history")
  public ResponseEntity<?> getMyTransactionHistory() {
    // 1. Get the currently authenticated user's username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to access transaction history by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.debug("Received request for transaction history for user: {}", currentUsername);

    try {
      // 2. Call the service to get the history
      List<TransactionResponse> history = transactionService.getTransactionHistory(currentUsername);

      // 3. Return the history list with 200 OK status
      return ResponseEntity.ok(history);

    } catch (Exception e) {
      // Catch any unexpected errors during history retrieval
      log.error("Error fetching transaction history for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching transaction history.");
    }
  }
  // --- End New Endpoint ---

}
