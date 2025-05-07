package com.credigo.backend.controller;

import com.credigo.backend.dto.LoginRequest;
import com.credigo.backend.dto.LoginResponse;
import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.dto.UserResponse;
import com.credigo.backend.dto.ProfileUpdateRequest;
import com.credigo.backend.entity.User;
import com.credigo.backend.repository.UserRepository; // Import UserRepository
import com.credigo.backend.security.jwt.JwtTokenProvider; // Import JwtTokenProvider
import com.credigo.backend.service.UserService;
import com.credigo.backend.service.ActivityLogService; // Import ActivityLogService
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional; // Import Optional
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @GetMapping("/test")
  public String hello() {
    return "Hello, World!";
  }
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);
  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider; // Inject JwtTokenProvider
  private final UserRepository userRepository; // Inject UserRepository to get full User details
  private final PasswordEncoder passwordEncoder;
  private final ActivityLogService activityLogService; // Add ActivityLogService

  @Autowired
  public AuthController(
      @Qualifier("mainUserServiceImpl") UserService userService, // Add the @Qualifier annotation here
      AuthenticationManager authenticationManager,
      JwtTokenProvider jwtTokenProvider, // Add to constructor
      UserRepository userRepository, // Add to constructor
      PasswordEncoder passwordEncoder, // Add to constructor
      ActivityLogService activityLogService) { // Add to constructor
    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider; // Initialize
    this.userRepository = userRepository; // Initialize
    this.passwordEncoder = passwordEncoder;
    this.activityLogService = activityLogService; // Initialize
  }

  // --- Registration Endpoint (with activity logging) ---
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest,
                                        HttpServletRequest request) {
    log.info("Received registration request for username: {}", registrationRequest.getUsername());
    try {
      User newUser = userService.registerUser(registrationRequest);
      UserResponse userResponse = mapToUserResponse(newUser);
      
      // Log the registration activity
      activityLogService.logActivity(
          newUser.getId(),
          "User registered",
          "REGISTRATION",
          "User registered with email: " + newUser.getEmail(),
          request.getRemoteAddr()
      );
      
      log.info("User registration successful for username: {}", registrationRequest.getUsername());
      return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      log.error("Registration failed for username {}: {}", registrationRequest.getUsername(), e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(e.getMessage());
    }
  }

  // --- Updated Login Endpoint with activity logging ---
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                           HttpServletRequest request) {
    log.info("Received login request for user: {}", loginRequest.getUsernameOrEmail());
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsernameOrEmail(),
              loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("User '{}' authenticated successfully.", loginRequest.getUsernameOrEmail());

      String jwt = jwtTokenProvider.generateToken(authentication);
      log.debug("Generated JWT for user '{}'", loginRequest.getUsernameOrEmail());

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String username = userDetails.getUsername();

      Optional<User> userOptional = userRepository.findByUsername(username);
      if (userOptional.isEmpty()) {
        log.error("Authenticated user '{}' not found in repository!", username);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error retrieving user details after login.");
      }
      User loggedInUser = userOptional.get();

      // Log the successful login activity
      activityLogService.logActivity(
          loggedInUser.getId(),
          "User logged in",
          "LOGIN",
          "User logged in successfully",
          request.getRemoteAddr()
      );

      Set<String> roles = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      LoginResponse loginResponse = new LoginResponse(
          "Login successful!",
          loggedInUser.getId(),
          loggedInUser.getUsername(),
          loggedInUser.getEmail(),
          roles,
          jwt
      );

      return ResponseEntity.ok(loginResponse);

    } catch (Exception e) {
      log.error("Authentication failed for user {}: {}", loginRequest.getUsernameOrEmail(), e.getMessage());
      
      // Try to find user to log failed attempt (if user exists)
      Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsernameOrEmail());
      if (userOptional.isEmpty()) {
          userOptional = userRepository.findByEmail(loginRequest.getUsernameOrEmail());
      }
      
      // Log failed login attempt if user exists
      userOptional.ifPresent(user -> {
          activityLogService.logActivity(
              user.getId(),
              "Failed login attempt",
              "LOGIN_FAILED",
              "Authentication failed: " + e.getMessage(),
              request.getRemoteAddr()
          );
      });
      
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: Invalid credentials");
    }
  }
  
  // --- Update Profile Endpoint (with activity logging) ---
  @PutMapping("/profile/{userId}")
  @PreAuthorize("#userId == authentication.principal.id")
  public ResponseEntity<?> updateProfile(@PathVariable Long userId, 
                                        @Valid @RequestBody ProfileUpdateRequest updateRequest,
                                        HttpServletRequest request) {
    log.info("Received profile update request for user ID: {}", userId);
    
    try {
      Optional<User> userOptional = userRepository.findById(userId.intValue());
      if (userOptional.isEmpty()) {
        log.error("User not found with ID: {}", userId);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("User not found");
      }

      User existingUser = userOptional.get();
      
      // Store original values for logging changes
      String originalUsername = existingUser.getUsername();
      String updatedFields = "";

      // Update username if provided and different
      if (updateRequest.getUsername() != null && 
          !updateRequest.getUsername().isEmpty() && 
          !updateRequest.getUsername().equals(existingUser.getUsername())) {
        
        if (userRepository.findByUsername(updateRequest.getUsername()).isPresent()) {
          return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body("Username already taken");
        }
        existingUser.setUsername(updateRequest.getUsername());
        updatedFields += "username, ";
      }

      // Update password if provided
      if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
        existingUser.setPasswordHash(passwordEncoder.encode(updateRequest.getPassword()));
        updatedFields += "password, ";
      }

      // Update phone number if provided
      if (updateRequest.getPhoneNumber() != null) {
        if (!updateRequest.getPhoneNumber().matches("^[+]?[0-9]{10,13}$")) {
          return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body("Invalid phone number format");
        }
        existingUser.setPhoneNumber(updateRequest.getPhoneNumber());
        updatedFields += "phone number, ";
      }

      User updatedUser = userService.updateUser(userId, existingUser);
      
      // Log the profile update activity
      if (!updatedFields.isEmpty()) {
        updatedFields = updatedFields.substring(0, updatedFields.length() - 2); // Remove trailing comma and space
        activityLogService.logActivity(
            updatedUser.getId(),
            "Profile updated",
            "PROFILE_UPDATE",
            "User updated profile fields: " + updatedFields,
            request.getRemoteAddr()
        );
      }
      
      UserResponse userResponse = mapToUserResponse(updatedUser);
      
      log.info("Profile updated successfully for user ID: {}", userId);
      return ResponseEntity.ok(userResponse);

    } catch (Exception e) {
      log.error("Profile update failed for user ID {}: {}", userId, e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("Failed to update profile: " + e.getMessage());
    }
  }

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
    Set<String> roleNames = user.getRoles().stream().map(r -> "ROLE_" +
    r.getRoleName()).collect(Collectors.toSet());
    dto.setRoles(roleNames);
    return dto;
  }

  //findByuser by name
  @GetMapping("/findByName/{username}")
  public ResponseEntity<UserResponse> findByUsername(@PathVariable String username) {
    log.info("Finding user by username: {}", username);
    
    try {
      User user = userService.findByUsername(username);
      if (user == null) {
        log.info("User not found with username: {}", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
      
      UserResponse userResponse = userService.mapToUserResponse(user);
      return ResponseEntity.ok(userResponse);
    } catch (Exception e) {
      log.error("Error finding user by username: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
  
  //find user by email
  @GetMapping("/findByEmail/{email}")
  public ResponseEntity<UserResponse> findByEmail(@PathVariable String email) {
    log.info("Finding user by email: {}", email);
    
    try {
      User user = userService.findByEmail(email);
      if (user == null) {
        log.info("User not found with email: {}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
      
      UserResponse userResponse = userService.mapToUserResponse(user);
      return ResponseEntity.ok(userResponse);
    } catch (Exception e) {
      log.error("Error finding user by email: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
