package com.credigo.backend.controller;

import com.credigo.backend.dto.ApiError;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.ProductPurchaseRequest;
import com.credigo.backend.dto.WalletBalanceRequest;
import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.entity.Wallet;
import com.credigo.backend.service.PaymentService;
import com.credigo.backend.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * REST Controller for managing User Wallets and initiating payments.
 */
@RestController
@RequestMapping("/api/wallet") // Base path for wallet-related endpoints
public class WalletController {

  private static final Logger log = LoggerFactory.getLogger(WalletController.class);

  private final WalletService walletService;
  private final PaymentService paymentService; // Inject PaymentService

  // Updated Constructor Injection
  @Autowired
  public WalletController(WalletService walletService, PaymentService paymentService) {
    this.walletService = walletService;
    this.paymentService = paymentService; // Initialize PaymentService
  }

  /**
   * Endpoint to get the wallet details for the currently authenticated user.
   *
   * @return ResponseEntity containing WalletResponse DTO on success, or an error
   *         status.
   */
  @GetMapping("/me") // Endpoint like /api/wallet/me
  public ResponseEntity<?> getCurrentUserWallet() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to access wallet by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.debug("Received request to get wallet for authenticated user: {}", currentUsername);

    try {
      WalletResponse walletResponse = walletService.getWalletByUsername(currentUsername);
      return ResponseEntity.ok(walletResponse);
    } catch (RuntimeException e) {
      log.error("Error fetching wallet for user {}: {}", currentUsername, e.getMessage());
      // Return 404 Not Found if the service threw an exception indicating not found
      // Use more specific exception handling in a real app
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching wallet.");
    } catch (Exception e) {
      log.error("Unexpected error fetching wallet for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }
  }

  // --- New Endpoint to Create Payment Intent for Wallet Top-up ---
  /**
   * Endpoint for an authenticated user to initiate a wallet top-up
   * by creating a PayMongo Payment Intent.
   *
   * @param topUpRequest DTO containing the amount to top up.
   * @return ResponseEntity containing PaymentResponse DTO (with client_secret) on
   *         success, or an error status.
   */
  @PostMapping("/create-payment-intent")
  public ResponseEntity<?> createWalletTopUpIntent(@Valid @RequestBody WalletTopUpRequest topUpRequest) {
    // 1. Get the currently authenticated user's username
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to create payment intent by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }
    String currentUsername = authentication.getName();
    log.info("Received request to create payment intent for user: {}, amount: {}", currentUsername,
        topUpRequest.getAmount());

    try {
      // 2. Call the payment service to create the PayMongo Payment Intent
      PaymentResponse paymentResponse = paymentService.createWalletTopUpPaymentIntent(topUpRequest, currentUsername);

      // 3. Return the PaymentResponse (containing client secret) with 200 OK status
      log.info("PaymentIntent created successfully for user: {}", currentUsername);
      return ResponseEntity.ok(paymentResponse);

    } catch (IllegalArgumentException e) {
      // Catch specific validation errors from the service (e.g., invalid amount)
      log.warn("Invalid request for create payment intent for user {}: {}", currentUsername, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (RuntimeException e) {
      // Catch other exceptions from the PaymentService (e.g., Stripe API errors)
      log.error("Failed to create PaymentIntent for user {}: {}", currentUsername, e.getMessage());
      // Return 500 Internal Server Error or a more specific error if available
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to initiate payment: " + e.getMessage());
    } catch (Exception e) {
      // Catch any other unexpected errors
      log.error("Unexpected error during PaymentIntent creation for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An unexpected error occurred while initiating payment.");
    }
  }

  /**
   * Update wallet balance
   * 
   * @param walletId The ID of the wallet to update
   * @param request The request containing the new balance value
   * @return ResponseEntity with the updated wallet data
   */
  @PutMapping("/{walletId}/balance")
  @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isWalletOwner(#walletId, authentication.name)")
  public ResponseEntity<?> updateWalletBalance(
          @PathVariable Long walletId,
          @Valid @RequestBody WalletBalanceRequest request) {
      
      log.info("Updating balance for wallet {}: new balance = {}", walletId, request.getBalance());
      
      try {
          // Get the current wallet
          Wallet wallet = walletService.getWalletById(walletId);
          if (wallet == null) {
              return ResponseEntity.notFound().build();
          }
          
          // Validate new balance (optional additional validation)
          BigDecimal newBalance = request.getBalance();
          if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
              return ResponseEntity
                  .badRequest()
                  .body(new ApiError("Balance cannot be negative"));
          }
          
          // Set new balance and update
          wallet.setBalance(newBalance);
          wallet.setLastUpdatedAt(new Date());
          
          // Save the updated wallet
          Wallet updatedWallet = walletService.updateWallet(wallet);
          
          // Return updated wallet
          return ResponseEntity.ok(updatedWallet);
      } catch (Exception e) {
          log.error("Error updating wallet balance: {}", e.getMessage(), e);
          return ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new ApiError("Error updating wallet: " + e.getMessage()));
      }
  }

  /**
   * Process a product purchase by deducting from wallet balance and creating a transaction
   * 
   * @param request The product purchase details
   * @return ResponseEntity with the transaction details or error message
   */
  @PostMapping("/purchase")
  public ResponseEntity<?> processPurchase(@Valid @RequestBody ProductPurchaseRequest request) {
      // Get the currently authenticated user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      
      // Log authentication details for debugging
      log.info("Authentication: isAuthenticated={}, principal={}, authorities={}, details={}", 
              authentication != null ? authentication.isAuthenticated() : "null", 
              authentication != null ? authentication.getPrincipal() : "null",
              authentication != null ? authentication.getAuthorities() : "null",
              authentication != null ? authentication.getDetails() : "null");
      
      if (authentication == null || !authentication.isAuthenticated() 
          || "anonymousUser".equals(authentication.getPrincipal())) {
          log.error("Unauthorized access attempt to purchase endpoint");
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError("User not authenticated"));
      }
      
      String username = authentication.getName();
      log.info("Processing purchase for user {}, product: {}, price: {}", 
              username, request.getProductName(), request.getPrice());
      
      try {
          // Process the purchase
          Map<String, Object> result = walletService.processPurchase(
              username, 
              request.getProductId(), 
              request.getProductName(), 
              request.getPrice(), 
              request.getDescription());
          
          // Return success response
          return ResponseEntity.ok(result);
      } catch (IllegalArgumentException e) {
          // Bad request errors (insufficient funds, invalid amounts, etc.)
          log.warn("Invalid purchase request for user {}: {}", username, e.getMessage());
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiError(e.getMessage()));
      } catch (Exception e) {
          // Other errors
          log.error("Error processing purchase for user {}: {}", username, e.getMessage(), e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new ApiError("Error processing purchase: " + e.getMessage()));
      }
  }

  // --- TODO: Add Stripe Webhook Handler Endpoint (moved to PaymentController)
  // ---

}
