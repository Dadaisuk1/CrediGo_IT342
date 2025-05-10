package com.credigo.backend.service;

import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.entity.Role;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.Wallet; // Ensure Wallet is imported if used here
import com.credigo.backend.repository.RoleRepository;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.repository.WalletRepository; // Ensure WalletRepository is imported if used here
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// --- Add these required imports ---
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.stream.Collectors;
// --- Other necessary imports ---
import java.math.BigDecimal;
import java.util.Set;
import com.credigo.backend.service.NotificationService;
import com.credigo.backend.service.EmailService;
import com.credigo.backend.service.NotificationService.NotificationType;

@Service
public class UserServiceImpl implements UserService, UserDetailsService { // Implement UserDetailsService

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final WalletRepository walletRepository; // Make sure this is injected if needed
  private final NotificationService notificationService;
  private final EmailService emailService;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      WalletRepository walletRepository,
      NotificationService notificationService,
      EmailService emailService) { // Ensure WalletRepository is in constructor
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.walletRepository = walletRepository; // Ensure WalletRepository is initialized
    this.notificationService = notificationService;
    this.emailService = emailService;
  }

  @Override
  @Transactional
  public User registerUser(UserRegistrationRequest registrationRequest) {
    // (Keep existing registration logic including wallet creation)
    log.info("Attempting to register user with username: {}", registrationRequest.getUsername());

    if (userRepository.existsByUsername(registrationRequest.getUsername())) {
      log.warn("Registration failed: Username '{}' is already taken!", registrationRequest.getUsername());
      throw new RuntimeException(
          "Registration failed: Username '" + registrationRequest.getUsername() + "' is already taken!");
    }
    if (userRepository.existsByEmail(registrationRequest.getEmail())) {
      log.warn("Registration failed: Email '{}' is already in use!", registrationRequest.getEmail());
      throw new RuntimeException(
          "Registration failed: Email '" + registrationRequest.getEmail() + "' is already in use!");
    }

    User user = new User();
    user.setUsername(registrationRequest.getUsername());
    user.setEmail(registrationRequest.getEmail());
    user.setPasswordHash(passwordEncoder.encode(registrationRequest.getPassword()));

    if (registrationRequest.getPhoneNumber() != null && !registrationRequest.getPhoneNumber().isBlank()) {
      user.setPhoneNumber(registrationRequest.getPhoneNumber());
    }
    if (registrationRequest.getDateOfBirth() != null) {
      user.setDateOfBirth(registrationRequest.getDateOfBirth());
    }

    String defaultRoleName = "USER";
    Role userRole = roleRepository.findByRoleName(defaultRoleName)
        .orElseThrow(() -> {
          log.error("CRITICAL: Default role '{}' not found in database!", defaultRoleName);
          return new RuntimeException(
              "Error: Default role '" + defaultRoleName + "' not found in database. " +
                  "Ensure roles are initialized on application startup.");
        });

    user.setRoles(Set.of(userRole));
    log.debug("Assigning role '{}' to user {}", defaultRoleName, user.getUsername());

    User savedUser = userRepository.save(user);
    log.info("User entity saved with ID: {}", savedUser.getId());

    Wallet newWallet = new Wallet();
    newWallet.setUser(savedUser);
    newWallet.setBalance(BigDecimal.ZERO);
    walletRepository.save(newWallet);
    log.info("Created wallet for user ID: {}", savedUser.getId());

    // Send welcome email
    emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

    // Send welcome notification
    notificationService.sendSuccessNotification(
        savedUser.getId().toString(),
        "Welcome to CrediGo! Your account has been created successfully."
    );

    log.info("Successfully registered user and created wallet for ID: {}", savedUser.getId());
    return savedUser;
  }

  // --- Implementation of UserDetailsService ---
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    log.debug("Attempting to load user by username or email: {}", usernameOrEmail);

    User user = userRepository.findByUsername(usernameOrEmail)
        .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
            .orElseThrow(() -> {
              log.warn("User not found with username or email: {}", usernameOrEmail);
              // Use the imported UsernameNotFoundException
              return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }));

    log.debug("User found: {}", user.getUsername());

    // Use the imported GrantedAuthority and SimpleGrantedAuthority
    Set<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
        .collect(Collectors.toSet()); // Use the imported Collectors

    log.debug("User authorities: {}", authorities);

    // Return Spring Security's User object (implements UserDetails)
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPasswordHash(),
        authorities);
  }

  // List all users
  @Override
  public java.util.List<User> findAllUsers() {
    return userRepository.findAll();
  }

  // Create user (admin)
  @Override
  @Transactional
  public User createUser(User user) {
    if (userRepository.existsByUsername(user.getUsername())) {
      throw new RuntimeException("Username already exists");
    }
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new RuntimeException("Email already exists");
    }
    // Hash password if provided
    if (user.getPasswordHash() != null) {
      user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    }
    // Assign default USER role if not set
    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      Role userRole = roleRepository.findByRoleName("USER")
        .orElseThrow(() -> new RuntimeException("Default role not found"));
      user.setRoles(Set.of(userRole));
    }
    // Create wallet if not present
    if (user.getWallet() == null) {
      Wallet wallet = new Wallet();
      wallet.setBalance(BigDecimal.ZERO);
      wallet.setUser(user);
      user.setWallet(wallet);
    }
    return userRepository.save(user);
  }

  // Update user (admin)
  @Override
  @Transactional
  public User updateUser(Long id, User updatedUser) {
    User user = userRepository.findById(id.intValue())
      .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    user.setUsername(updatedUser.getUsername());
    user.setEmail(updatedUser.getEmail());
    user.setPhoneNumber(updatedUser.getPhoneNumber());
    user.setDateOfBirth(updatedUser.getDateOfBirth());
    // Update active status if provided
    if (updatedUser.getActive() != null) {
      user.setActive(updatedUser.getActive());
    }
    // Optionally update wallet balance if present
    if (updatedUser.getBalance() != null) {
      Wallet wallet = walletRepository.findByUser_Id(user.getId()).orElse(null);
      if (wallet != null) {
        wallet.setBalance(updatedUser.getBalance());
        walletRepository.save(wallet);
      }
    }
    // Only update password if provided
    if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
    }
    // Optionally update roles if provided
    if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
      user.setRoles(updatedUser.getRoles());
    }
    return userRepository.save(user);
  }

  // Delete user (admin)
  @Override
  @Transactional
  public void deleteUser(Long id) {
    User user = userRepository.findById(id.intValue())
      .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    userRepository.delete(user);
  }

  // Promote user to admin
  @Override
  @Transactional
  public void promoteToAdmin(Long userId) {
    User user = userRepository.findById(userId.intValue())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    Role adminRole = roleRepository.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found in database!"));
    user.getRoles().add(adminRole);
    userRepository.save(user);
    log.info("Promoted user {} to ADMIN", user.getUsername());
  }

  // Demote user to regular user
  @Override
  @Transactional
  public void demoteToUser(Long userId) {
    User user = userRepository.findById(userId.intValue())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    Role adminRole = roleRepository.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found in database!"));
    if (!user.getRoles().remove(adminRole)) {
      throw new RuntimeException("User does not have ADMIN role");
    }
    userRepository.save(user);
    log.info("Demoted user {} to regular USER", user.getUsername());
  }

  // Map User to UserResponse DTO
  @Override
  public com.credigo.backend.dto.UserResponse mapToUserResponse(User user) {
    if (user == null) return null;
    com.credigo.backend.dto.UserResponse dto = new com.credigo.backend.dto.UserResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setPhoneNumber(user.getPhoneNumber());
    dto.setDateOfBirth(user.getDateOfBirth());
    dto.setCreatedAt(user.getCreatedAt());
    // Add roles as a Set<String>
    java.util.Set<String> roles = user.getRoles() != null ? user.getRoles().stream().map(r -> r.getRoleName()).collect(java.util.stream.Collectors.toSet()) : new java.util.HashSet<>();
    dto.setRoles(roles);
    dto.setActive(user.getActive());
    // Fetch wallet balance
    java.math.BigDecimal balance = java.math.BigDecimal.ZERO;
    if (user.getWallet() != null && user.getWallet().getBalance() != null) {
      balance = user.getWallet().getBalance();
    }
    dto.setBalance(balance);
    return dto;
  }

  // Comment out the resetPassword method
  /*
  @Override
  public void resetPassword(String email) {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

      String resetToken = generateResetToken();
      user.setResetToken(resetToken);
      user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
      userRepository.save(user);

      String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;
  //    emailService.sendPasswordResetEmail(email, resetLink);

      notificationService.sendInfoNotification(
          user.getId().toString(),
          "A password reset link has been sent to your email."
      );
  }
  */

  // Find and comment out or remove the method that uses UserUpdateDTO (around line 301)
  /*
  public User updateUserProfile(UserUpdateDTO userUpdateDTO) {
      // Method implementation
  }
  */
}
