package com.credigo.backend.repository;

import com.credigo.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
  Optional<Wallet> findByUser_Id(Integer userId);

  Optional<Wallet> findByUser_Username(String username);
}
