package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Import LocalDate for date of birth
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // "users" table name
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash; // Hashed password

  // Optional Fields
  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

  // Relationships
  @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>(); // Initialize the set

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Wallet wallet;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Transaction> transactions;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<WishlistItem> wishlistItems;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Review> reviews;

  // Transient field for balance (for admin DTO transfer)
  @Transient
  private java.math.BigDecimal balance;

  public java.math.BigDecimal getBalance() { return balance; }
  public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  // Helper method (optional)
  public void addRole(Role role) {
    this.roles.add(role);
  }
}
