package com.credigo.backend.controller;

import com.credigo.backend.dto.PaymentResponse; // Import PaymentResponse
import com.credigo.backend.dto.WalletTopUpRequest; // Import WalletTopUpRequest
import com.credigo.backend.service.PaymentService; // Import PaymentService
import com.credigo.backend.service.WalletService; // Import WalletService
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook; // Import Webhook utility
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
 * REST Controller for handling incoming Stripe Webhooks and other payment
 * operations.
 */
@RestController
@RequestMapping("/api/payments") // Base path for payment-related endpoints
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

  @Value("${stripe.webhook.secret}") // Inject webhook secret from properties
  private String endpointSecret;

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
   * by creating a Stripe Payment Intent.
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
      // Call the payment service to create the Stripe Payment Intent
      PaymentResponse paymentResponse = paymentService.createWalletTopUpPaymentIntent(topUpRequest, currentUsername);

      // Return the PaymentResponse (containing client secret) with 200 OK status
      log.info("PaymentIntent created successfully for user: {}", currentUsername);
      return ResponseEntity.ok(paymentResponse);

    } catch (RuntimeException e) {
      // Catch exceptions from the PaymentService (e.g., Stripe API errors)
      log.error("Failed to create PaymentIntent for user {}: {}", currentUsername, e.getMessage());
      // Return 500 Internal Server Error or a more specific error if available
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  /**
   * Endpoint to receive webhook events from Stripe.
   *
   * @param payload   The raw request body (JSON payload from Stripe).
   * @param sigHeader The value of the Stripe-Signature header.
   * @return ResponseEntity indicating success (200 OK) or failure.
   */
  @PostMapping("/stripe/webhook")
  public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
      @RequestHeader("Stripe-Signature") String sigHeader) {
    // Debug log to confirm endpoint is hit
    log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    log.error("!!! STRIPE WEBHOOK ENDPOINT /api/payments/stripe/webhook WAS HIT !!!");
    log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    log.debug("Received Stripe webhook event payload (first 100 chars): {}",
        payload.substring(0, Math.min(payload.length(), 100))); // Log part of payload

    if (endpointSecret == null || endpointSecret.isBlank()) {
      log.error("Webhook processing failed: Stripe webhook secret is not configured.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured on server.");
    }

    Event event;

    try {
      // 1. Verify the event signature
      event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
      log.debug("Webhook signature verified. Event ID: {}, Type: {}", event.getId(), event.getType());

    } catch (SignatureVerificationException e) {
      // Invalid signature
      log.warn("Webhook signature verification failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      log.error("Webhook payload parsing failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
    }

    // 2. Deserialize the event data object
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    StripeObject stripeObject = null;
    if (dataObjectDeserializer.getObject().isPresent()) {
      stripeObject = dataObjectDeserializer.getObject().get();
    } else {
      log.error("Webhook event data deserialization failed for event type: {}. Check Stripe API version compatibility.",
          event.getType());
      return ResponseEntity.ok("Webhook received but data deserialization failed.");
    }

    // 3. Handle the event based on its type
    switch (event.getType()) {
      case "payment_intent.succeeded":
        PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
        log.info("Received PaymentIntent succeeded event! ID: {}", paymentIntent.getId());
        // --- Call service to handle successful payment ---
        handleSuccessfulPaymentIntent(paymentIntent); // Call the helper method
        break;

      case "payment_intent.payment_failed":
        PaymentIntent failedPaymentIntent = (PaymentIntent) stripeObject;
        log.warn("PaymentIntent failed! ID: {}, Reason: {}", failedPaymentIntent.getId(),
            failedPaymentIntent.getLastPaymentError() != null ? failedPaymentIntent.getLastPaymentError().getMessage()
                : "Unknown");
        // TODO: Handle failed payment (e.g., notify user, update internal status)
        break;

      // ... handle other event types as needed (e.g., charge.refunded) ...

      default:
        log.warn("Unhandled Stripe event type received: {}", event.getType());
    }

    // 4. Acknowledge receipt of the event to Stripe successfully
    return ResponseEntity.ok("Webhook received successfully");
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
