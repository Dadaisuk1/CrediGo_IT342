package com.credigo.backend.controller;

import com.credigo.backend.dto.LoginRequest;
import com.credigo.backend.dto.LoginResponse;
import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.dto.UserResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.repository.UserRepository; // Import UserRepository
import com.credigo.backend.security.jwt.JwtTokenProvider; // Import JwtTokenProvider
import com.credigo.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional; // Import Optional
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);
  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider; // Inject JwtTokenProvider
  private final UserRepository userRepository; // Inject UserRepository to get full User details
  private final RememberMeServices rememberMeServices; // Remember Me services

  @Autowired
  public AuthController(UserService userService,
      AuthenticationManager authenticationManager,
      JwtTokenProvider jwtTokenProvider,
      UserRepository userRepository,
      RememberMeServices rememberMeServices) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
    this.rememberMeServices = rememberMeServices;
  }

  // --- Registration Endpoint (Keep as is) ---
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

  // --- Updated Login Endpoint ---
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request,
        HttpServletResponse response) {

    log.info("Received login request for user: {}", loginRequest.getUsernameOrEmail());
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getUsernameOrEmail(),
              loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("User '{}' authenticated successfully.", loginRequest.getUsernameOrEmail());

      // Handle Remember Me if requested
      if (loginRequest.getRememberMe() != null && loginRequest.getRememberMe()) {
        log.info("Remember Me requested for user: {}", loginRequest.getUsernameOrEmail());
        rememberMeServices.loginSuccess(request, response, authentication);
      }

      // *** Generate the JWT Token ***
      String jwt = jwtTokenProvider.generateToken(authentication);
      log.debug("Generated JWT for user '{}'", loginRequest.getUsernameOrEmail());

      // Get user details from the authenticated principal
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String username = userDetails.getUsername();

      // --- Fetch full User entity to get all details for the response ---
      Optional<User> userOptional = userRepository.findByUsername(username);
      if (userOptional.isEmpty()) {
        // This should ideally not happen if authentication succeeded
        log.error("Authenticated user '{}' not found in repository!", username);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error retrieving user details after login.");
      }
      User loggedInUser = userOptional.get();
      // --- End Fetch full User ---

      Set<String> roles = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      // Create the response DTO including the token
      LoginResponse loginResponse = new LoginResponse(
          "Login successful!",
          loggedInUser.getId(), // Include ID
          loggedInUser.getUsername(),
          loggedInUser.getEmail(), // Include Email
          roles,
          jwt // *** Include the generated token ***
      );

      return ResponseEntity.ok(loginResponse);

    } catch (Exception e) {
      log.error("Authentication failed for user {}: {}", loginRequest.getUsernameOrEmail(), e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: Invalid credentials");
    }
  }
  // --- End Updated Login Endpoint ---

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
}
