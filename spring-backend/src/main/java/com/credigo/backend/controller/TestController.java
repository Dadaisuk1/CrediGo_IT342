package com.credigo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Import
import org.springframework.security.core.context.SecurityContextHolder; // Import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/hello")
  public ResponseEntity<String> helloAuthenticatedUser() {
    // Get authentication details from the security context (set by JWT filter)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentPrincipalName = authentication.getName();
    return ResponseEntity.ok("Hello, authenticated user: " + currentPrincipalName);
  }
}
