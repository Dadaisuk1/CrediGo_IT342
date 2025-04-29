package com.credigo.backend.controller;

import com.credigo.backend.dto.PaymentResponse; // Import PaymentResponse
import com.credigo.backend.dto.WalletTopUpRequest; // Import WalletTopUpRequest
import com.credigo.backend.service.PaymentService; // Import PaymentService
import com.credigo.backend.service.WalletService; // Import WalletService

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Import RequestHeader, PostMapping etc.

import java.math.BigDecimal; // Import BigDecimal

/**
 * REST Controller for handling incoming PayMongo Webhooks and other payment operations.
 */
@RestController
@RequestMapping("/api/payments") // Base path for payment-related endpoints
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

  @Value("${paymongo.secret.key}") // Inject PayMongo secret key from properties
  private String paymongoSecretKey; // Used for API calls and optionally for webhook validation (if needed)

  private final WalletService walletService; // Inject WalletService
  private final PaymentService paymentService; // Inject PaymentService (needed for create-payment-intent)

  // Updated Constructor to inject WalletService and PaymentService
  @Autowired
  public PaymentController(WalletService walletService, PaymentService paymentService) {
    this.walletService = walletService;
    this.paymentService = paymentService;
  }

  /**
   * Endpoint for an authenticated user to initiate a wallet top-up
   * by creating a PayMongo Payment Intent.
   *
   * @param topUpRequest DTO containing the amount to top up.
   * @return ResponseEntity containing PaymentResponse DTO (with client_secret) on
   *         success, or an error status.
   */
  @PostMapping("/create-payment-intent") // Ensure this endpoint exists if called
  public ResponseEntity<?> createWalletTopUpIntent(@Valid @RequestBody WalletTopUpRequest topUpRequest) {
    // Get the currently authenticated user's username
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
      // Call the payment service to create the PayMongo Payment Intent
      PaymentResponse paymentResponse = paymentService.createWalletTopUpPaymentIntent(topUpRequest, currentUsername);

      // Return the PaymentResponse (containing client secret) with 200 OK status
      log.info("PaymentIntent created successfully for user: {}", currentUsername);
      return ResponseEntity.ok(paymentResponse);

    } catch (RuntimeException e) {
      // Catch exceptions from the PaymentService (e.g., PayMongo API errors)
      log.error("Failed to create PaymentIntent for user {}: {}", currentUsername, e.getMessage());
      // Return 500 Internal Server Error or a more specific error if available
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  /**
   * Endpoint to receive webhook events from PayMongo.
   *
   * @param payload The raw request body (JSON payload from PayMongo).
   * @return ResponseEntity indicating success (200 OK) or failure.
   */
  @PostMapping("/paymongo/webhook")
  public ResponseEntity<String> handlePayMongoWebhook(@RequestBody String payload) {
    log.info("PayMongo webhook endpoint hit. Payload (first 200 chars): {}", payload.substring(0, Math.min(payload.length(), 200)));

    try {
      // Parse the incoming JSON payload
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      java.util.Map<String, Object> webhookEvent = objectMapper.readValue(payload, java.util.Map.class);

      // PayMongo webhook event structure: { data: { id, type, attributes: {...} } }
      java.util.Map<String, Object> data = (java.util.Map<String, Object>) webhookEvent.get("data");
      if (data == null) {
        log.error("PayMongo webhook: 'data' field missing in payload.");
        return ResponseEntity.ok("Webhook received but missing data field.");
      }
      String eventType = (String) data.get("type");
      java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) data.get("attributes");
      if (attributes == null) {
        log.error("PayMongo webhook: 'attributes' missing in event data.");
        return ResponseEntity.ok("Webhook received but missing attributes.");
      }

      // Handle only payment_intent.succeeded events for wallet top-up
      if ("payment_intent.succeeded".equals(eventType)) {
        String paymentIntentId = (String) data.get("id");
        Long amount = attributes.get("amount") instanceof Integer ? ((Integer) attributes.get("amount")).longValue() : (Long) attributes.get("amount");
        String currency = (String) attributes.get("currency");
        String description = attributes.get("description") != null ? (String) attributes.get("description") : "Wallet top-up via PayMongo";
        java.util.Map<String, Object> metadata = attributes.get("metadata") instanceof java.util.Map ? (java.util.Map<String, Object>) attributes.get("metadata") : null;
        String username = metadata != null ? (String) metadata.get("credigo_username") : null;
        String type = metadata != null ? (String) metadata.get("transaction_type") : null;

        log.info("PayMongo webhook: payment_intent.succeeded for user={}, paymentIntentId={}, amount={}, currency={}", username, paymentIntentId, amount, currency);

        // Only process wallet top-ups
        if ("wallet_topup".equals(type) && username != null && !username.isBlank()) {
          java.math.BigDecimal amountDecimal = java.math.BigDecimal.valueOf(amount).divide(new java.math.BigDecimal("100"));
          try {
            walletService.addFundsToWallet(username, amountDecimal, paymentIntentId, description);
            log.info("Successfully credited wallet for user {} via PayMongo paymentIntent {}", username, paymentIntentId);
          } catch (Exception e) {
            log.error("Failed to credit wallet for user {}: {}", username, e.getMessage(), e);
          }
        } else {
          log.warn("Ignoring PayMongo payment_intent.succeeded: metadata missing or not a wallet top-up. Metadata: {}", metadata);
        }
      } else {
        log.warn("Unhandled PayMongo event type: {}", eventType);
      }
    } catch (Exception e) {
      log.error("Error processing PayMongo webhook: {}", e.getMessage(), e);
    }
    // Always acknowledge webhook receipt (PayMongo expects 2xx)
    return ResponseEntity.ok("Webhook received");
  }

  /**
   * Helper method to process successful PaymentIntent events.
   * Extracts necessary information and calls WalletService to add funds.
   * Includes basic error catching to ensure webhook always returns 200 OK to
   * Stripe.
   *
   * @param paymentIntent The successful PaymentIntent object from Stripe.
   */
  private void handleSuccessfulPaymentIntent(PaymentIntent paymentIntent) {
    try {
      // Extract metadata - IMPORTANT: Ensure metadata was set correctly when creating
      // PaymentIntent
      String username = paymentIntent.getMetadata().get("credigo_username");
      String type = paymentIntent.getMetadata().get("transaction_type");
      String paymentIntentId = paymentIntent.getId(); // Get the ID for idempotency

      log.debug("Processing successful PaymentIntent: ID={}, Type={}, Username={}",
          paymentIntentId, type, username);

      // Check if this is a wallet top-up and username is present
      if ("wallet_topup".equals(type) && username != null && !username.isBlank()) {

        // Convert amount back from smallest currency unit (e.g., centavos)
        // Ensure this matches the currency used when creating the PaymentIntent (e.g.,
        // PHP)
        // Use paymentIntent.getAmountReceived() for accuracy if available and
        // appropriate
        long amountReceived = paymentIntent.getAmountReceived() != null ? paymentIntent.getAmountReceived()
            : paymentIntent.getAmount();
        BigDecimal amount = BigDecimal.valueOf(amountReceived).divide(new BigDecimal("100")); // Assuming 100 subunits
        String description = "Wallet top-up via Stripe";

        log.info("Attempting to add funds via WalletService: User={}, Amount={}, PI_ID={}",
            username, amount, paymentIntentId);

        // Call the wallet service to add funds (includes idempotency check)
        walletService.addFundsToWallet(username, amount, paymentIntentId, description);

        log.info("Successfully processed wallet top-up for user {} via PaymentIntent {}", username, paymentIntentId);

      } else {
        log.warn(
            "Ignoring successful PaymentIntent {} - metadata missing or incorrect type (Expected 'wallet_topup' with 'credigo_username'). Metadata: {}",
            paymentIntentId, paymentIntent.getMetadata());
      }
    } catch (Exception e) {
      // Catch potential errors during wallet update (e.g., user not found, database
      // issue, invalid amount)
      // Log error, but crucially DO NOT re-throw the exception here.
      // We must return 200 OK to Stripe for the webhook acknowledgment.
      // Implement separate monitoring/alerting/retry logic for failed processing if
      // needed.
      log.error("Error handling successful PaymentIntent {}: {}",
          (paymentIntent != null ? paymentIntent.getId() : "UNKNOWN"), e.getMessage(), e);
    }
  }
}
