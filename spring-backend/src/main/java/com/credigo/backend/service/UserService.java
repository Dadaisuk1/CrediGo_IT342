package com.credigo.backend.service;

import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.entity.User; // Or return a DTO like UserResponse

public interface UserService {
  /**
   * Registers a new user in the system.
   *
   * @param registrationRequest DTO containing registration details.
   * @return The newly created User entity.
   * @throws RuntimeException if username or email already exists, or default role
   **/
  User registerUser(UserRegistrationRequest registrationRequest);

  // List all users
  java.util.List<User> findAllUsers();

  // Create user (admin)
  User createUser(User user);

  // Update user (admin)
  User updateUser(Long id, User user);

  // Delete user (admin)
  void deleteUser(Long id);

  // Promote user to admin
  void promoteToAdmin(Long userId);

  // Demote user to regular user
  void demoteToUser(Long userId);

  // Map User entity to UserResponse DTO
  com.credigo.backend.dto.UserResponse mapToUserResponse(User user);

  
  User findByUsername(String username);

  User findByEmail(String email);
}