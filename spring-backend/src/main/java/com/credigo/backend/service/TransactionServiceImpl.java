package com.credigo.backend.service;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse;
import com.credigo.backend.entity.*; // Import necessary entities
import com.credigo.backend.repository.*; // Import necessary repositories
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for atomicity

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation of the TransactionService interface.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final WalletTransactionRepository walletTransactionRepository;
  // Inject other repositories if needed

  @Autowired
  public TransactionServiceImpl(UserRepository userRepository,
      ProductRepository productRepository,
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      WalletTransactionRepository walletTransactionRepository) {
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.walletTransactionRepository = walletTransactionRepository;
  }

  @Override
  @Transactional // Ensures the entire purchase process is one atomic transaction
  public TransactionResponse processPurchase(PurchaseRequest purchaseRequest, String username) {
    log.info("Processing purchase request for user: {}, product ID: {}", username, purchaseRequest.getProductId());

    // 1. Fetch User
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    // 2. Fetch Product
    Product product = productRepository.findById(purchaseRequest.getProductId())
        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + purchaseRequest.getProductId()));

    // 3. Validate Product
    if (!product.isAvailable()) {
      throw new RuntimeException("Product '" + product.getName() + "' is currently unavailable.");
    }

    // 4. Fetch Wallet
    Wallet wallet = walletRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + username)); // Should not happen if

    // 5. Calculate Total Cost
    BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(purchaseRequest.getQuantity()));

    // 6. Check Sufficient Funds
    if (wallet.getBalance().compareTo(totalCost) < 0) {
      // Balance is less than total cost
      throw new RuntimeException(
          "Insufficient funds. Wallet balance: " + wallet.getBalance() + ", Required: " + totalCost);
    }

    // --- If all checks pass, proceed with transaction ---

    // 7. Deduct Funds from Wallet
    wallet.setBalance(wallet.getBalance().subtract(totalCost));
    Wallet updatedWallet = walletRepository.save(wallet); // Save updated balance
    log.info("Deducted {} from wallet ID: {}. New balance: {}", totalCost, wallet.getId(), updatedWallet.getBalance());

    // 8. Record Wallet Transaction (Debit)
    WalletTransaction walletTx = new WalletTransaction();
    walletTx.setWallet(updatedWallet);
    walletTx.setTransactionType(WalletTransactionType.PURCHASE_DEDUCTION);
    walletTx.setAmount(totalCost.negate()); // Store deduction as negative amount
    walletTx.setDescription("Purchase of " + product.getName() + " (x" + purchaseRequest.getQuantity() + ")");
    // We will link this walletTx to the main Transaction later if needed
    walletTransactionRepository.save(walletTx);
    log.debug("Saved wallet transaction for purchase.");

    // 9. Create and Save Purchase Transaction Record (Initially Pending/Processing)
    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setProduct(product);
    transaction.setQuantity(purchaseRequest.getQuantity());
    transaction.setPurchasePrice(product.getPrice()); // Price at time of purchase
    transaction.setTotalAmount(totalCost);
    transaction.setPaymentMethod(PaymentMethod.WALLET); // Assuming wallet payment for now
    transaction.setStatus(TransactionStatus.PROCESSING); // Start as PROCESSING
    transaction.setGameAccountId(purchaseRequest.getGameAccountId());
    transaction.setGameServerId(purchaseRequest.getGameServerId());
    transaction.setWalletTransaction(walletTx); // Link the wallet deduction (optional FK)

    Transaction savedTransaction = transactionRepository.save(transaction);
    log.info("Saved initial purchase transaction with ID: {}", savedTransaction.getId());

    // --- Placeholder for External API Call ---
    boolean topUpSuccess = callExternalTopUpAPI(savedTransaction); // Simulate API call

    // 10. Update Transaction Status based on API result
    if (topUpSuccess) {
      savedTransaction.setStatus(TransactionStatus.COMPLETED);
      savedTransaction.setExternalApiStatus(ExternalApiStatus.SUCCESS); // Optional external status
      log.info("External top-up successful for transaction ID: {}", savedTransaction.getId());
    } else {
      savedTransaction.setStatus(TransactionStatus.FAILED);
      savedTransaction.setExternalApiStatus(ExternalApiStatus.FAILED); // Optional external status
      log.error("External top-up failed for transaction ID: {}", savedTransaction.getId());
      // IMPORTANT: Consider refunding logic here! If API fails, should funds be
      // returned?
      // refundPurchase(savedTransaction, walletTx, updatedWallet); // Example refund
      // call
      // For now, we just mark as failed. Refund logic is complex.
    }
    Transaction finalTransaction = transactionRepository.save(savedTransaction); // Save final status

    // 11. Map final transaction to Response DTO
    return mapToResponseDto(finalTransaction);
  }

  // --- Mock External API Call ---
  private boolean callExternalTopUpAPI(Transaction transaction) {
    // TODO: Replace this with actual integration logic for the game top-up API
    log.warn("SIMULATING external API call for transaction ID: {}", transaction.getId());
    // Simulate success or failure randomly or based on some condition for testing
    boolean success = Math.random() > 0.1; // Simulate 90% success rate
    try {
      Thread.sleep(1000); // Simulate API call delay
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    log.warn("SIMULATED external API call result: {}", success ? "SUCCESS" : "FAILURE");
    return success;
  }

  // --- Helper Mapping Method ---
  private TransactionResponse mapToResponseDto(Transaction entity) {
    if (entity == null)
      return null;
    TransactionResponse dto = new TransactionResponse();
    dto.setTransactionId(entity.getId());
    dto.setQuantity(entity.getQuantity());
    dto.setPurchasePrice(entity.getPurchasePrice());
    dto.setTotalAmount(entity.getTotalAmount());
    dto.setGameAccountId(entity.getGameAccountId());
    dto.setGameServerId(entity.getGameServerId());
    dto.setStatus(entity.getStatus());
    dto.setTransactionTimestamp(entity.getTransactionTimestamp());

    // Add status message based on status
    switch (entity.getStatus()) {
      case COMPLETED:
        dto.setStatusMessage("Purchase successful.");
        break;
      case PROCESSING:
        dto.setStatusMessage("Purchase is processing.");
        break;
      case FAILED:
        dto.setStatusMessage("Purchase failed. Please contact support if funds were deducted.");
        break;
      case PENDING:
        dto.setStatusMessage("Purchase pending.");
        break;
      case REFUNDED:
        dto.setStatusMessage("Purchase refunded.");
        break;
      default:
        dto.setStatusMessage("Unknown status.");
    }

    if (entity.getUser() != null) {
      dto.setUserId(entity.getUser().getId());
      dto.setUsername(entity.getUser().getUsername());
    }
    if (entity.getProduct() != null) {
      dto.setProductId(entity.getProduct().getId());
      dto.setProductName(entity.getProduct().getName());
    }

    return dto;
  }

  // --- TODO: Implement refund logic if needed ---
  // private void refundPurchase(Transaction failedTransaction, WalletTransaction
  // debitTx, Wallet wallet) { ... }
}
