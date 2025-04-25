package com.credigo.backend.controller;

import com.credigo.backend.dto.LoginRequest; // Import LoginRequest
import com.credigo.backend.dto.LoginResponse; // Import LoginResponse
import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.dto.UserResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import
import org.springframework.security.core.Authentication; // Import
import org.springframework.security.core.context.SecurityContextHolder; // Import
import org.springframework.security.core.userdetails.UserDetails; // Import
import org.springframework.security.core.GrantedAuthority; // Import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set; // Import Set
import java.util.stream.Collectors; // Import Collectors

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);
  private final UserService userService;
  private final AuthenticationManager authenticationManager; // Inject AuthenticationManager

  @Autowired
  public AuthController(UserService userService, AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.authenticationManager = authenticationManager; // Initialize AuthenticationManager
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
    log.info("Received registration request for username: {}", registrationRequest.getUsername());
    try {
      User newUser = userService.registerUser(registrationRequest);
      UserResponse userResponse = mapToUserResponse(newUser);
      log.info("User registration successful for username: {}", registrationRequest.getUsername());
      return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      log.error("Registration failed for username {}: {}", registrationRequest.getUsername(), e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(e.getMessage());
    }
  }

  // --- New Login Endpoint ---
  /**
   * Handles POST requests to authenticate a user (login).
   *
   * @param loginRequest The login credentials from the request body.
   * @return ResponseEntity containing LoginResponse on success, or an error
   *         message on failure.
   */
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    log.info("Received login request for user: {}", loginRequest.getUsernameOrEmail());
    try {
      // Attempt authentication using the provided credentials
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsernameOrEmail(),
              loginRequest.getPassword()));

      // If authentication is successful, set the authentication object in the
      // security context
      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("User '{}' authenticated successfully.", loginRequest.getUsernameOrEmail());

      // Get user details from the authenticated principal
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      // Extract necessary details to build the response
      // NOTE: Since UserDetails only guarantees getUsername(), getPassword(),
      // getAuthorities(),
      // you might need to fetch the full User entity again if you need more details
      // like email/id.
      // However, for simplicity here, we'll use what's available in UserDetails.
      String username = userDetails.getUsername();
      Set<String> roles = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      // TODO: Fetch full User entity if more details are needed for the response
      // User user = userRepository.findByUsername(username).orElse(null); // Example

      // Create the response DTO (add JWT token later)
      LoginResponse loginResponse = new LoginResponse(
          "Login successful!",
          null, // TODO: Add user ID if fetched
          username,
          null, // TODO: Add email if fetched
          roles
      // null // Placeholder for JWT token
      );

      return ResponseEntity.ok(loginResponse);

    } catch (Exception e) {
      // Catch authentication exceptions (e.g., BadCredentialsException)
      log.error("Authentication failed for user {}: {}", loginRequest.getUsernameOrEmail(), e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: Invalid credentials");
    }
  }
  // --- End Login Endpoint ---

  // Helper method to map User Entity to UserResponse DTO
  private UserResponse mapToUserResponse(User user) {
    if (user == null)
      return null;
    UserResponse dto = new UserResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setPhoneNumber(user.getPhoneNumber());
    dto.setDateOfBirth(user.getDateOfBirth());
    dto.setCreatedAt(user.getCreatedAt());
    // Example if returning roles:
    // Set<String> roleNames = user.getRoles().stream().map(r -> "ROLE_" +
    // r.getRoleName()).collect(Collectors.toSet());
    // dto.setRoles(roleNames);
    return dto;
  }
}
