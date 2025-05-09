package com.credigo.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Simple DTO for API error responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String message;
    
    // Add timestamp
    private long timestamp = System.currentTimeMillis();
    
    // Constructor with just the message
    public ApiError(String message) {
        this.message = message;
    }
} 