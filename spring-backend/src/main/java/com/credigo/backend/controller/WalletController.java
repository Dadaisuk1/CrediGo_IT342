package com.credigo.backend.controller;

import com.credigo.backend.dto.WalletResponse;
import com.credigo.backend.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Consider importing this
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing User Wallets.
 */
@RestController
@RequestMapping("/api/wallet") // Base path for wallet-related endpoints
public class WalletController {

  private static final Logger log = LoggerFactory.getLogger(WalletController.class);

  private final WalletService walletService;

  // Constructor Injection
  @Autowired
  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  /**
   * Endpoint to get the wallet details for the currently authenticated user.
   *
   * @return ResponseEntity containing WalletResponse DTO on success, or an error
   *         status.
   */
  @GetMapping("/me") // Endpoint like /api/wallet/me
  public ResponseEntity<?> getCurrentUserWallet() {
    // 1. Get the currently authenticated user's principal from the Security Context
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Check if user is authenticated
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      log.warn("Attempt to access wallet by unauthenticated user.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }

    // Get the username (principal identifier)
    String currentUsername = authentication.getName();
    log.debug("Received request to get wallet for authenticated user: {}", currentUsername);

    try {
      // 2. Call the service to get the wallet details
      WalletResponse walletResponse = walletService.getWalletByUsername(currentUsername);

      // 3. Return the wallet details with 200 OK status
      return ResponseEntity.ok(walletResponse);

    } catch (RuntimeException e) { // Catch exceptions like wallet not found
      log.error("Error fetching wallet for user {}: {}", currentUsername, e.getMessage());
      // Return 404 Not Found if the service threw an exception indicating not found
      // You might want more specific exception handling here
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      // Catch any other unexpected errors
      log.error("Unexpected error fetching wallet for user {}: {}", currentUsername, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }
  }

  // --- TODO: Add endpoints for wallet operations like adding funds ---
  // @PostMapping("/deposit") ...

}
