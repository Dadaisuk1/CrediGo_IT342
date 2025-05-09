package com.credigo.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be 8+ characters long and contain at least one uppercase letter, one lowercase letter, one number and one special character"
    )
    private String password;

    @Pattern(
        regexp = "^[+]?[0-9]{10,13}$",
        message = "Phone number must be between 10 and 13 digits, optionally starting with +"
    )
    private String phoneNumber;
} 