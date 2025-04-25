package com.credigo.backend.repository;

import com.credigo.backend.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {
  List<WalletTransaction> findByWallet_IdOrderByTransactionTimestampDesc(Integer walletId);

  List<WalletTransaction> findByWallet_User_IdOrderByTransactionTimestampDesc(Integer userId);
}
