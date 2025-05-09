package com.credigo.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Integer id;
    private Integer walletId;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionTimestamp;
}
