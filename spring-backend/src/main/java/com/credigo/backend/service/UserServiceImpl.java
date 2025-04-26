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

@Service
public class UserServiceImpl implements UserService, UserDetailsService { // Implement UserDetailsService

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final WalletRepository walletRepository; // Make sure this is injected if needed

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      WalletRepository walletRepository) { // Ensure WalletRepository is in constructor
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.walletRepository = walletRepository; // Ensure WalletRepository is initialized
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
}
