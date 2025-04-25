package com.credigo.backend.repository;

import com.credigo.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
  List<Transaction> findByUser_IdOrderByTransactionTimestampDesc(Integer userId);

  List<Transaction> findByUser_UsernameOrderByTransactionTimestampDesc(String username);
  // Add more custom queries as needed, e.g., find by status, product, etc.
}
