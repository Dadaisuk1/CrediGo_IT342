package com.credigo.backend.repository;

import com.credigo.backend.entity.Transaction;
import com.credigo.backend.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
// Remove: import org.springframework.transaction.TransactionStatus;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
  List<Transaction> findByUser_IdOrderByTransactionTimestampDesc(Integer userId);

  List<Transaction> findByUser_UsernameOrderByTransactionTimestampDesc(String username);

  // Change the type of the 'status' parameter here
  boolean existsByUser_IdAndProduct_IdAndStatus(Integer userId, Integer productId,
      com.credigo.backend.entity.TransactionStatus status);
  // Add more custom queries as needed, e.g., find by status, product, etc.
}
