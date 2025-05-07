package com.credigo.backend.controller;

import com.credigo.backend.dto.UserResponse;
import com.credigo.backend.entity.User;
import com.credigo.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(@Qualifier("mainUserServiceImpl") UserService userService) {
        this.userService = userService;
    }

    // List all users (admin only)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(userService::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // Create user (admin)
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(userService.mapToUserResponse(created));
    }

    // Update user (admin)
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(userService.mapToUserResponse(updated));
    }

    // Delete user (admin)
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Promote user to admin
    @PostMapping("/users/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long id) {
        try {
            userService.promoteToAdmin(id);
            return ResponseEntity.ok("User promoted to admin.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Demote admin to user
    @PostMapping("/users/{id}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> demoteToUser(@PathVariable Long id) {
        try {
            userService.demoteToUser(id);
            return ResponseEntity.ok("User demoted to regular user.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin dashboard stats endpoint
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminStats() {
        // Total users
        long totalUsers = userService.findAllUsers().size();
        // Active users (for demo: count users created in last 30 days)
        long activeUsers = userService.findAllUsers().stream()
            .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(30)))
            .count();
        // TODO: Replace with real service/repo calls for products/transactions
        long totalProducts = 0;
        long totalTransactions = 0;
        java.util.List<Object> recentTransactions = java.util.Collections.emptyList();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalTransactions", totalTransactions);
        stats.put("recentTransactions", recentTransactions);
        return ResponseEntity.ok(stats);
    }
}
