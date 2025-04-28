package com.credigo.backend.service;

import com.credigo.backend.entity.*;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.repository.WalletRepository;
import com.credigo.backend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private WalletTransactionRepository walletTransactionRepository;

  @Autowired
  private UserRepository userRepository;

  @Transactional
  public void addFundsToWallet(String username, BigDecimal amount, String description) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));

    Wallet wallet = user.getWallet();
    if (wallet == null) {
      wallet = new Wallet();
      wallet.setUser(user);
      wallet.setBalance(BigDecimal.ZERO);
      wallet = walletRepository.save(wallet);
    }

    wallet.setBalance(wallet.getBalance().add(amount));
    walletRepository.save(wallet);

    WalletTransaction walletTransaction = new WalletTransaction();
    walletTransaction.setWallet(wallet);
    walletTransaction.setAmount(amount);
    walletTransaction.setType(WalletTransactionType.TOP_UP);
    walletTransaction.setDescription(description);
    walletTransactionRepository.save(walletTransaction);
  }
}
