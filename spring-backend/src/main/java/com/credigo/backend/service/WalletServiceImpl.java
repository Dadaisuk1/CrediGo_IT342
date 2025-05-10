package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
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
  public void addFundsToWallet(String username, double amount, String paymentIntentId) {
    // Convert double to BigDecimal
    BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);

    // Call the main implementation with a default description
    addFundsToWallet(username, amountBigDecimal, paymentIntentId,
        "Wallet top-up via admin confirmation");
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

    // 3. Find or create the user's wallet
    Wallet wallet;
    try {
      wallet = walletRepository.findByUser_Username(username)
          .orElseThrow(() -> {
            log.error("Cannot add funds: Wallet not found for username: {}", username);
            return new RuntimeException("Wallet not found for user: " + username);
          });
    } catch (RuntimeException e) {
      // If we get here, the wallet was not found
      log.warn("Wallet not found for user: {}. Error message: {}", username, e.getMessage());
      throw new RuntimeException("Cannot add funds: Wallet not found for user: " + username +
          ". Please ensure the username is correct and the user has a wallet.");
    }

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
