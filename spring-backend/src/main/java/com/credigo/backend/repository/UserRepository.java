package com.credigo.backend.repository;

import com.credigo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.Optional; // Optional if adding custom methods like findByUsername

public interface UserRepository extends JpaRepository<User, Integer> {
  // Spring Data JPA provides findById, findAll, save, deleteById, etc.
  // automatically.
  // You can add custom query methods later if needed, e.g.:
  // Optional<User> findByUsername(String username);
  // Optional<User> findByEmail(String email);
}
