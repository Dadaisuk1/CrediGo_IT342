package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.dto.WalletTransactionResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.Wallet;
import com.credigo.backend.entity.WalletTransaction;
import com.credigo.backend.entity.WalletTransactionType;
import com.credigo.backend.repository.WalletRepository;
import com.credigo.backend.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the WalletService interface.
 */
@Service
public class WalletServiceImpl implements WalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

  private final WalletRepository walletRepository;
  private final WalletTransactionRepository walletTransactionRepository; // Inject WalletTransactionRepository

  // Updated Constructor Injection
  @Autowired
  public WalletServiceImpl(WalletRepository walletRepository,
      WalletTransactionRepository walletTransactionRepository) {
    this.walletRepository = walletRepository;
    this.walletTransactionRepository = walletTransactionRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public WalletResponse getWalletByUsername(String username) {
    log.debug("Fetching wallet for username: {}", username);
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          log.warn("Wallet not found for username: {}", username);
          return new RuntimeException("Wallet not found for user: " + username);
        });
    return mapToResponseDto(wallet);
  }

  @Override
  @Transactional // Ensure atomicity
  public void addFundsToWallet(String username, BigDecimal amount, String paymentIntentId, String description) {
    log.info("Attempting to add funds for user: {}, amount: {}, paymentIntentId: {}", username, amount,
        paymentIntentId);

    // 1. Validate amount
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("Invalid deposit amount: {}", amount);
      throw new IllegalArgumentException("Deposit amount must be positive.");
    }

    // 2. Check for duplicate processing using paymentIntentId (Idempotency)
    // We store the paymentIntentId in the WalletTransaction description or a
    // dedicated field if added.
    // This requires a custom query in WalletTransactionRepository.
    boolean alreadyProcessed = walletTransactionRepository.existsByDescriptionContaining(paymentIntentId);
    if (alreadyProcessed) {
      log.warn("Duplicate webhook event ignored for paymentIntentId: {}", paymentIntentId);

      return;
    }

    // 3. Find the user's wallet
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          log.error("Cannot add funds: Wallet not found for username: {}", username);
          return new RuntimeException("Wallet not found for user: " + username);
        });

    // 4. Add funds to the balance
    wallet.setBalance(wallet.getBalance().add(amount));
    Wallet updatedWallet = walletRepository.save(wallet); // Save updated balance
    log.info("Added {} to wallet ID: {}. New balance: {}", amount, wallet.getId(), updatedWallet.getBalance());

    // 5. Record Wallet Transaction (Deposit)
    WalletTransaction depositTx = new WalletTransaction();
    depositTx.setWallet(updatedWallet);
    depositTx.setTransactionType(WalletTransactionType.DEPOSIT); // Use DEPOSIT type
    depositTx.setAmount(amount); // Store deposit as positive amount
    // Include paymentIntentId in description for idempotency check
    depositTx.setDescription(description + " (PayMongo PI: " + paymentIntentId + ")");
    // depositTx.setPaymentIntentId(paymentIntentId); // If dedicated field added
    walletTransactionRepository.save(depositTx);
    log.debug("Saved DEPOSIT wallet transaction for paymentIntentId: {}", paymentIntentId);
  }

  @Override
  @Transactional
  public void recordPendingTransaction(String username, BigDecimal amount, String transactionType,
                                     String description, String paymentId, String status) {
    log.info("Recording pending transaction for user: {}, amount: {}, paymentId: {}",
            username, amount, paymentId);

    // Check for duplicate processing using paymentId (Idempotency)
    boolean alreadyProcessed = walletTransactionRepository.existsByDescriptionContaining(paymentId);
    if (alreadyProcessed) {
      log.warn("Duplicate pending transaction ignored for paymentId: {}", paymentId);
      return;
    }

    // Find the user's wallet
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          log.error("Cannot record pending transaction: Wallet not found for username: {}", username);
          return new RuntimeException("Wallet not found for user: " + username);
        });

    // Create a pending transaction record
    WalletTransaction pendingTx = new WalletTransaction();
    pendingTx.setWallet(wallet);

    // Determine transaction type
    if ("WALLET_TOPUP".equals(transactionType)) {
      pendingTx.setTransactionType(WalletTransactionType.PENDING_DEPOSIT);
    } else {
      pendingTx.setTransactionType(WalletTransactionType.PENDING);
    }

    // Record the amount (but don't affect wallet balance yet)
    pendingTx.setAmount(amount);

    // Include paymentId in description for tracking and idempotency
    pendingTx.setDescription(description + " (Status: " + status + ")");

    // Save the pending transaction
    walletTransactionRepository.save(pendingTx);
    log.info("Saved PENDING transaction with paymentId: {}", paymentId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<WalletTransactionResponse> getWalletTransactions(String username) {
    log.info("Fetching wallet transactions for user: {}", username);

    // Find user's wallet
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          log.error("Cannot get transactions: Wallet not found for username: {}", username);
          return new RuntimeException("Wallet not found for user: " + username);
        });

    // Get transactions for wallet
    List<WalletTransaction> transactions = walletTransactionRepository
        .findByWallet_IdOrderByTransactionTimestampDesc(wallet.getId());

    log.info("Found {} wallet transactions for user {}", transactions.size(), username);

    // Map to response DTOs
    return transactions.stream()
        .map(this::mapTransactionToDto)
        .collect(Collectors.toList());
  }

  // Helper method to map WalletTransaction to DTO
  private WalletTransactionResponse mapTransactionToDto(WalletTransaction transaction) {
    WalletTransactionResponse dto = new WalletTransactionResponse();
    dto.setId(transaction.getId());
    dto.setWalletId(transaction.getWallet().getId());
    dto.setTransactionType(transaction.getTransactionType().name());
    dto.setAmount(transaction.getAmount());
    dto.setDescription(transaction.getDescription());
    dto.setTransactionTimestamp(transaction.getTransactionTimestamp());
    return dto;
  }

  // --- Helper Mapping Method ---
  private WalletResponse mapToResponseDto(Wallet entity) {
    if (entity == null)
      return null;
    WalletResponse dto = new WalletResponse();
    dto.setWalletId(entity.getId());
    dto.setBalance(entity.getBalance());
    dto.setLastUpdatedAt(entity.getLastUpdatedAt());
    User user = entity.getUser();
    if (user != null) {
      dto.setUserId(user.getId());
      dto.setUsername(user.getUsername());
    } else {
      log.warn("Wallet with ID {} found but has no associated user.", entity.getId());
      dto.setUserId(null);
      dto.setUsername("Unknown");
    }
    return dto;
  }
}
