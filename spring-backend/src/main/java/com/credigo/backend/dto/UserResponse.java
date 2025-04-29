package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Integer id; // The user's ID from the database
  private String username;
  private String email;
  private String phoneNumber;
  private LocalDate dateOfBirth;
  private LocalDateTime createdAt;
  // Notice: NO password field here!

  private java.util.Set<String> roles;

  public java.util.Set<String> getRoles() {
    return roles;
  }

  public void setRoles(java.util.Set<String> roles) {
    this.roles = roles;
  }
}
