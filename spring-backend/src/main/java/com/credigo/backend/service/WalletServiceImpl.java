package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.Wallet;
import com.credigo.backend.entity.WalletTransaction;
import com.credigo.backend.entity.WalletTransactionType;
import com.credigo.backend.entity.Transaction;
import com.credigo.backend.entity.TransactionStatus;
import com.credigo.backend.entity.PaymentMethod;
import com.credigo.backend.entity.Product;
import com.credigo.backend.exception.ResourceNotFoundException;
import com.credigo.backend.repository.WalletRepository;
import com.credigo.backend.repository.WalletTransactionRepository;
import com.credigo.backend.repository.TransactionRepository;
import com.credigo.backend.repository.ProductRepository;
import com.credigo.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the WalletService interface.
 */
@Service
public class WalletServiceImpl implements WalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

  private final WalletRepository walletRepository;
  private final WalletTransactionRepository walletTransactionRepository;
  private final TransactionRepository transactionRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  // Updated Constructor Injection
  @Autowired
  public WalletServiceImpl(WalletRepository walletRepository,
      WalletTransactionRepository walletTransactionRepository,
      TransactionRepository transactionRepository,
      ProductRepository productRepository,
      UserRepository userRepository) {
    this.walletRepository = walletRepository;
    this.walletTransactionRepository = walletTransactionRepository;
    this.transactionRepository = transactionRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
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
  public Wallet getWalletById(Long walletId) {
    // Use Optional handling to return null if not found
    return walletRepository.findById(walletId).orElse(null);
  }

  @Override
  public Wallet updateWallet(Wallet wallet) {
    // Ensure the wallet exists before updating
    if (wallet.getId() == null || !walletRepository.existsById(wallet.getId())) {
      throw new ResourceNotFoundException("Wallet not found with id: " + wallet.getId());
    }
    
    // Save and return the updated wallet
    return walletRepository.save(wallet);
  }
  
  @Override
  @Transactional
  public Map<String, Object> processPurchase(String username, Long productId, String productName, 
      BigDecimal price, String description) {
    log.info("Processing purchase for user: {}, product: {}, price: {}", username, productName, price);
    
    // 1. Validate price
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("Invalid purchase price: {}", price);
      throw new IllegalArgumentException("Purchase price must be positive");
    }
    
    // 2. Get user's wallet and user entity
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.error("User not found: {}", username);
          return new RuntimeException("User not found: " + username);
        });
        
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          log.error("Cannot process purchase: Wallet not found for username: {}", username);
          return new RuntimeException("Wallet not found for user: " + username);
        });
    
    // 3. Check if user has sufficient balance
    if (wallet.getBalance().compareTo(price) < 0) {
      log.warn("Insufficient funds for purchase. User: {}, Balance: {}, Price: {}", 
          username, wallet.getBalance(), price);
      throw new IllegalArgumentException("Insufficient funds. Your balance: ₱" + wallet.getBalance() + 
          ", Required: ₱" + price);
    }
    
    // 4. Get product entity (if it exists)
    Product product = null;
    try {
      if (productId != null) {
        product = productRepository.findById(productId.intValue())
            .orElse(null);
      }
    } catch (Exception e) {
      log.warn("Error fetching product with ID {}: {}", productId, e.getMessage());
      // Continue with null product - we'll use the provided name and price
    }
    
    // 5. Deduct the purchase amount from wallet balance
    BigDecimal newBalance = wallet.getBalance().subtract(price);
    wallet.setBalance(newBalance);
    wallet.setLastUpdatedAt(new Date());
    
    // Save updated wallet
    Wallet updatedWallet = walletRepository.save(wallet);
    log.info("Deducted {} from wallet ID: {}. New balance: {}", 
        price, wallet.getId(), updatedWallet.getBalance());
    
    // 6. Create wallet transaction record
    WalletTransaction walletTx = new WalletTransaction();
    walletTx.setWallet(updatedWallet);
    walletTx.setTransactionType(WalletTransactionType.PURCHASE_DEDUCTION);
    walletTx.setAmount(price.negate()); // Store purchase as negative amount
    
    // Create meaningful description
    String txDescription = description;
    if (txDescription == null || txDescription.trim().isEmpty()) {
      txDescription = "Purchase of " + productName;
    }
    walletTx.setDescription(txDescription + " (ProductID: " + productId + ")");
    
    // Save wallet transaction
    WalletTransaction savedWalletTx = walletTransactionRepository.save(walletTx);
    log.info("Saved PURCHASE wallet transaction ID: {} for user: {}, product: {}", 
        savedWalletTx.getId(), username, productName);
    
    // 7. Create regular transaction record
    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setProduct(product); // May be null if product doesn't exist
    transaction.setQuantity(1); // Default to 1
    transaction.setPurchasePrice(price);
    transaction.setTotalAmount(price); // Same as price for quantity 1
    transaction.setPaymentMethod(PaymentMethod.WALLET);
    transaction.setStatus(TransactionStatus.COMPLETED); // Mark as completed immediately
    transaction.setGameAccountId(username); // Use username as gameAccountId if not provided
    transaction.setWalletTransaction(savedWalletTx); // Link to wallet transaction
    
    // Save transaction
    Transaction savedTransaction = transactionRepository.save(transaction);
    log.info("Saved regular transaction with ID: {} for user: {}", 
        savedTransaction.getId(), username);
    
    // 8. Return transaction details and updated balance
    Map<String, Object> result = new HashMap<>();
    result.put("transactionId", savedTransaction.getId());
    result.put("walletTransactionId", savedWalletTx.getId());
    result.put("productId", productId);
    result.put("productName", productName);
    result.put("price", price);
    result.put("transactionDate", new Date());
    result.put("newBalance", updatedWallet.getBalance());
    result.put("status", "success");
    
    return result;
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
