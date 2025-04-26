package com.credigo.backend.service;

import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
// Import User entity if needed to pass customer info to Stripe
// import com.credigo.backend.entity.User;
// import com.credigo.backend.repository.UserRepository;
import com.stripe.Stripe; // Import Stripe static class
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct; // Use jakarta PostConstruct
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Correct import for @Value
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementation of the PaymentService interface using Stripe.
 */
@Service // Marks this as a Spring service component
public class PaymentServiceImpl implements PaymentService { // Ensure this implements PaymentService interface

  private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

  // Inject the Stripe Secret Key from application.properties
  @Value("${stripe.secret.key}")
  private String secretKey;

  // Optional: Inject UserRepository if you need user details (like email) for
  // Stripe Customer
  // private final UserRepository userRepository;

  // Constructor (inject repositories if needed)
  // public PaymentServiceImpl(UserRepository userRepository) {
  // this.userRepository = userRepository;
  // }
  public PaymentServiceImpl() {
    // Default constructor if no other dependencies needed initially
  }

  /**
   * Initializes the Stripe API key globally when the service is created.
   */
  @PostConstruct // Ensures this runs after the secretKey is injected
  public void init() {
    Stripe.apiKey = secretKey;
    log.info("Stripe API Key configured.");
    // You might want to log only a part of the key or just confirmation for
    // security
    // log.debug("Stripe Key Used: {}", secretKey != null ? secretKey.substring(0,
    // 10) + "..." : "null");
  }

  /**
   * Creates a Stripe Payment Intent for a wallet top-up request.
   *
   * @param topUpRequest DTO containing the amount to top up.
   * @param username     The username of the user initiating the top-up.
   * @return PaymentResponse DTO containing the client secret for the Payment
   *         Intent.
   * @throws RuntimeException if the Payment Intent creation fails.
   */
  @Override
  public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
    log.info("Creating PaymentIntent for user: {}, amount: {}", username, topUpRequest.getAmount());

    // Convert amount from BigDecimal (e.g., 199.00) to smallest currency unit
    // (e.g., 19900 centavos for PHP)
    long amountInCentavos = topUpRequest.getAmount().multiply(new BigDecimal("100")).longValueExact();
    String currency = "php"; // Set your currency code

    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(amountInCentavos)
        .setCurrency(currency)
        // *** CHANGED BACK HERE: Specify gcash and card ***
        .addPaymentMethodType("card")
        // .addPaymentMethodType("gcash")
        // .addPaymentMethodType("paypal")
        .putMetadata("credigo_username", username)
        .putMetadata("transaction_type", "wallet_topup")
        .build();

    try {
      // Create the PaymentIntent on Stripe's servers
      PaymentIntent paymentIntent = PaymentIntent.create(params);
      log.info("Successfully created PaymentIntent ID: {}", paymentIntent.getId());

      // Return the client_secret needed by the frontend
      return new PaymentResponse(paymentIntent.getClientSecret());

    } catch (StripeException e) {
      log.error("Stripe PaymentIntent creation failed for user {}: {}", username, e.getMessage());
      // Pass Stripe's specific error message back
      throw new RuntimeException("Failed to create payment intent: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error during PaymentIntent creation for user {}: {}", username, e.getMessage(), e);
      throw new RuntimeException("An unexpected error occurred while initiating payment.", e);
    }
  }

  // --- TODO: Implement helper method to find or create Stripe Customer if needed
  // ---
  // private String findOrCreateStripeCustomer(User user) throws StripeException {
  // ... }

  // --- TODO: Implement webhook handler for successful payments ---
  // public void handleStripeWebhook(Event event) { ... }

}
