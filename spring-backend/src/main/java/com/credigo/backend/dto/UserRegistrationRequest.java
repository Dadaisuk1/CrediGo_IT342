package com.credigo.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past; // For date of birth validation
import jakarta.validation.constraints.Pattern; // Optional: for phone number format
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Import LocalDate

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email should be valid")
  @Size(max = 255, message = "Email cannot exceed 255 characters")
  private String email;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  private String password;

  // Optional Fields
  // Optional: Add validation pattern for phone number if desired
  // @Pattern(regexp = "^(\\+?[0-9\\s\\-()]*)$", message = "Invalid phone number
  // format")
  @Size(max = 20, message = "Phone number cannot exceed 20 characters")
  private String phoneNumber; // Optional field

  @Past(message = "Date of birth must be in the past") // Ensures date is not in the future
  private LocalDate dateOfBirth; // Optional field

}
