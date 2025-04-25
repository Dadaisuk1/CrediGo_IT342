package com.credigo.backend.repository;

import com.credigo.backend.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Integer> {
  Optional<Platform> findByName(String name);
}
