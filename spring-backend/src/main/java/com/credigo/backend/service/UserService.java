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

  // Define other user-related service methods here later
}
