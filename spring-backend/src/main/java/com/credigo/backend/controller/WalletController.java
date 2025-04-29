package com.credigo.backend.controller;

import com.credigo.backend.dto.PaymentResponse; // Import PaymentResponse
import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.dto.WalletTopUpRequest; // Import WalletTopUpRequest
import com.credigo.backend.service.PaymentService; // Import PaymentService
import com.credigo.backend.service.WalletService;
import jakarta.validation.Valid; // Import Valid
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Import PostMapping

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

  // --- TODO: Add Stripe Webhook Handler Endpoint (moved to PaymentController)
  // ---

}
