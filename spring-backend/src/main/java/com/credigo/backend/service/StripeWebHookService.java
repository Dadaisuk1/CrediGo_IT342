package com.credigo.service;

import com.stripe.model.PaymentIntent;
import com.credigo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class StripeWebhookService {

  @Autowired
  private WalletService walletService;

  @Autowired
  private UserService userService;

  @Autowired
  private TransactionHistoryService transactionHistoryService;

  @Autowired
  private EmailNotificationService emailNotificationService;

  public void handleSuccessfulPaymentIntent(PaymentIntent paymentIntent) {
    try {
      String username = paymentIntent.getMetadata().get("credigo_username");
      String paymentIntentId = paymentIntent.getId();

      if (username == null || username.isBlank()) {
        System.out.println("PaymentIntent received with missing username, skipping processing.");
        return;
      }

      // Convert amount (Stripe sends amounts in cents)
      long amountReceived = paymentIntent.getAmountReceived() != null ? paymentIntent.getAmountReceived()
          : paymentIntent.getAmount();
      BigDecimal amount = BigDecimal.valueOf(amountReceived).divide(new BigDecimal("100"));

      // Add funds to wallet
      walletService.addFundsToWallet(username, amount, paymentIntentId, "Wallet top-up via Stripe");

      // Save to transaction history
      User user = userService.findByUsername(username);
      transactionHistoryService.saveTransactionHistory(user, amount, "Wallet top-up via Stripe");

      // Send confirmation email
      emailNotificationService.sendTopUpSuccessEmail(user.getEmail(), amount);

    } catch (Exception e) {
      System.err.println("Error processing successful PaymentIntent: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
