package com.credigo.backend.service;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.Wallet;
import com.credigo.backend.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Implementation of Wallet Service
@Service // Marks this as a Spring service component
public class WalletServiceImpl implements WalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

  private final WalletRepository walletRepository;

  // Constructor Injection
  @Autowired
  public WalletServiceImpl(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  @Override
  @Transactional(readOnly = true) // Read-only transaction for fetching data
  public WalletResponse getWalletByUsername(String username) {
    log.debug("Fetching wallet for username: {}", username);

    // Find the wallet using the custom repository method
    Wallet wallet = walletRepository.findByUser_Username(username)
        .orElseThrow(() -> {
          // This could mean the user doesn't exist OR the user exists but somehow doesn't
          // have a wallet
          // (which shouldn't happen if wallets are created during registration or lazily)
          log.warn("Wallet not found for username: {}", username);
          // Use a more specific exception like ResourceNotFoundException in a real app
          return new RuntimeException("Wallet not found for user: " + username);
        });

    // Map the found Wallet entity to the WalletResponse DTO
    return mapToResponseDto(wallet);
  }

  // --- Helper Mapping Method ---
  private WalletResponse mapToResponseDto(Wallet entity) {
    if (entity == null) {
      return null;
    }
    WalletResponse dto = new WalletResponse();
    dto.setWalletId(entity.getId());
    dto.setBalance(entity.getBalance());
    dto.setLastUpdatedAt(entity.getLastUpdatedAt());

    // Get user details from the associated User entity
    User user = entity.getUser();
    if (user != null) {
      dto.setUserId(user.getId());
      dto.setUsername(user.getUsername());
    } else {
      // Handle case where user might be null unexpectedly (shouldn't happen with
      // proper FKs)
      log.warn("Wallet with ID {} found but has no associated user.", entity.getId());
      dto.setUserId(null); // Or handle differently
      dto.setUsername("Unknown");
    }

    return dto;
  }

  // --- TODO: Implement methods for modifying wallet balance ---
  // e.g., addFunds(Integer userId, BigDecimal amount)
  // e.g., deductFunds(Integer userId, BigDecimal amount) - ensuring sufficient
  // balance

}
