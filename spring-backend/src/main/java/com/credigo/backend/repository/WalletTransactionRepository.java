package com.credigo.backend.repository;

import com.credigo.backend.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {
}
