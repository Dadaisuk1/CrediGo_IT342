package com.sia.credigo.model

import java.math.BigDecimal

/**
 * Data class for wallet top-up request that matches backend DTO.
 * Used for creating payment intents for wallet deposits.
 */
data class WalletTopUpRequest(
    val amount: BigDecimal,
    val description: String = "Wallet Top-up" // Optional field with default value
)
