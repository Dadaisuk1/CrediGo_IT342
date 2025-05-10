package com.credigo.backend.repository;

import com.credigo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);

  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);

  Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
