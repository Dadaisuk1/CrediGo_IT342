package com.credigo.backend.service;

import com.credigo.backend.dto.PurchaseRequest;
import com.credigo.backend.dto.TransactionResponse;
import com.credigo.backend.entity.*; // Import necessary entities
import com.credigo.backend.repository.*; // Import necessary repositories
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Import List
import java.util.stream.Collectors; // Import Collectors

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
  @Transactional
  public TransactionResponse processPurchase(PurchaseRequest purchaseRequest, String username) {
    // --- Keep existing purchase logic ---
    log.info("Processing purchase request for user: {}, product ID: {}", username, purchaseRequest.getProductId());
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));
    Product product = productRepository.findById(purchaseRequest.getProductId())
        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + purchaseRequest.getProductId()));
    if (!product.isAvailable()) {
      throw new RuntimeException("Product '" + product.getName() + "' is currently unavailable.");
    }
    Wallet wallet = walletRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + username));
    BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(purchaseRequest.getQuantity()));
    if (wallet.getBalance().compareTo(totalCost) < 0) {
      throw new RuntimeException(
          "Insufficient funds. Wallet balance: " + wallet.getBalance() + ", Required: " + totalCost);
    }
    wallet.setBalance(wallet.getBalance().subtract(totalCost));
    Wallet updatedWallet = walletRepository.save(wallet);
    log.info("Deducted {} from wallet ID: {}. New balance: {}", totalCost, wallet.getId(), updatedWallet.getBalance());
    WalletTransaction walletTx = new WalletTransaction();
    walletTx.setWallet(updatedWallet);
    walletTx.setTransactionType(WalletTransactionType.PURCHASE_DEDUCTION);
    walletTx.setAmount(totalCost.negate());
    walletTx.setDescription("Purchase of " + product.getName() + " (x" + purchaseRequest.getQuantity() + ")");
    walletTransactionRepository.save(walletTx);
    log.debug("Saved wallet transaction for purchase.");
    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setProduct(product);
    transaction.setQuantity(purchaseRequest.getQuantity());
    transaction.setPurchasePrice(product.getPrice());
    transaction.setTotalAmount(totalCost);
    transaction.setPaymentMethod(PaymentMethod.WALLET);
    transaction.setStatus(TransactionStatus.PROCESSING);
    transaction.setGameAccountId(purchaseRequest.getGameAccountId());
    transaction.setGameServerId(purchaseRequest.getGameServerId());
    transaction.setWalletTransaction(walletTx);
    Transaction savedTransaction = transactionRepository.save(transaction);
    log.info("Saved initial purchase transaction with ID: {}", savedTransaction.getId());
    boolean topUpSuccess = callExternalTopUpAPI(savedTransaction);
    if (topUpSuccess) {
      savedTransaction.setStatus(TransactionStatus.COMPLETED);
      savedTransaction.setExternalApiStatus(ExternalApiStatus.SUCCESS);
      log.info("External top-up successful for transaction ID: {}", savedTransaction.getId());
    } else {
      savedTransaction.setStatus(TransactionStatus.FAILED);
      savedTransaction.setExternalApiStatus(ExternalApiStatus.FAILED);
      log.error("External top-up failed for transaction ID: {}", savedTransaction.getId());
      // Consider refund logic here
    }
    Transaction finalTransaction = transactionRepository.save(savedTransaction);
    return mapToResponseDto(finalTransaction);
    // --- End existing purchase logic ---
  }

  /**
   * Retrieves the transaction history for the specified user.
   *
   * @param username The username of the user whose history is to be retrieved.
   * @return A list of TransactionResponse DTOs, ordered by most recent first.
   * @throws RuntimeException if the user is not found (shouldn't happen for
   *                          authenticated user).
   */
  @Override
  @Transactional(readOnly = true) // Read-only operation
  public List<TransactionResponse> getTransactionHistory(String username) {
    log.debug("Fetching transaction history for user: {}", username);

    // Use the custom repository method to find transactions by username, ordered by
    // timestamp
    // Ensure findByUser_UsernameOrderByTransactionTimestampDesc exists in
    // TransactionRepository
    List<Transaction> transactions = transactionRepository.findByUser_UsernameOrderByTransactionTimestampDesc(username);

    // Map the list of Transaction entities to a list of TransactionResponse DTOs
    List<TransactionResponse> transactionResponses = transactions.stream()
        .map(this::mapToResponseDto) // Reuse the existing mapping method
        .collect(Collectors.toList());

    log.debug("Found {} transactions for user: {}", transactionResponses.size(), username);
    return transactionResponses;
  }

  // --- Mock External API Call (Keep as is) ---
  private boolean callExternalTopUpAPI(Transaction transaction) {
    log.warn("SIMULATING external API call for transaction ID: {}", transaction.getId());
    boolean success = Math.random() > 0.1; // Simulate 90% success rate
    try {
      Thread.sleep(1000); // Simulate API call delay
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    log.warn("SIMULATED external API call result: {}", success ? "SUCCESS" : "FAILURE");
    return success;
  }

  // --- Helper Mapping Method (Keep as is) ---
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

}
