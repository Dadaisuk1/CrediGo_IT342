package com.credigo.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * DTO for wallet balance update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceRequest {
    
    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance must be greater than or equal to zero")
    private BigDecimal balance;
    
    private String description;
} 