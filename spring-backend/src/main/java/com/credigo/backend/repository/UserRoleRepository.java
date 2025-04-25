package com.credigo.backend.repository;

import com.credigo.backend.entity.UserRole;
import com.credigo.backend.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository for the join table entity (if specific queries needed on the join table itself)
// Often, direct interaction happens via User or Role entities and their collections.
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
  // Custom queries related to the join table if necessary
}
