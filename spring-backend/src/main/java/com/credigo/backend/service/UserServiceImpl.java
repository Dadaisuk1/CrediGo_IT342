package com.credigo.backend.service;

import com.credigo.backend.dto.UserRegistrationRequest;
import com.credigo.backend.entity.Role;
import com.credigo.backend.entity.User;
import com.credigo.backend.repository.RoleRepository;
import com.credigo.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Import UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors; // Import Collectors

@Service
public class UserServiceImpl implements UserService, UserDetailsService { // Implement UserDetailsService

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public User registerUser(UserRegistrationRequest registrationRequest) {
    // (Keep existing registration logic from previous step)
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
      log.debug("Setting phone number for user {}", user.getUsername());
    }
    if (registrationRequest.getDateOfBirth() != null) {
      user.setDateOfBirth(registrationRequest.getDateOfBirth());
      log.debug("Setting date of birth for user {}", user.getUsername());
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
    log.info("Successfully registered user with ID: {}", savedUser.getId());

    return savedUser;
  }

  // --- Implementation of UserDetailsService ---
  /**
   * Loads user-specific data. Spring Security uses this method during
   * authentication.
   * It locates the user based on the username or email provided.
   *
   * @param usernameOrEmail The username or email the user is trying to log in
   *                        with.
   * @return UserDetails object containing user info (username, password,
   *         authorities).
   * @throws UsernameNotFoundException if the user could not be found.
   */
  @Override
  @Transactional(readOnly = true) // Read-only transaction for loading user details
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    log.debug("Attempting to load user by username or email: {}", usernameOrEmail);

    // Try finding user by username OR email
    User user = userRepository.findByUsername(usernameOrEmail)
        .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
            .orElseThrow(() -> {
              log.warn("User not found with username or email: {}", usernameOrEmail);
              return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }));

    log.debug("User found: {}", user.getUsername());

    // Convert the user's Set<Role> into a Set<GrantedAuthority> for Spring Security
    Set<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName())) // Prefix roles with ROLE_ (standard
                                                                               // convention)
        .collect(Collectors.toSet());

    log.debug("User authorities: {}", authorities);

    // Return Spring Security's User object (implements UserDetails)
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), // Use username as the principal identifier
        user.getPasswordHash(), // Provide the HASHED password from the database
        authorities // Provide the user's roles/authorities
    );
  }
  // --- End Implementation of UserDetailsService ---

}
