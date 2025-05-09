package com.sia.credigo.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data class representing a user's wallet.
 * Matches the backend Wallet entity structure from the WalletResponse DTO.
 * 
 * Note: This represents the data returned by WalletResponse, not the internal
 * Wallet entity which would include a full User object.
 */
data class Wallet(
    val id: Int = 0,
    val userId: Int = 0, // Non-nullable to match backend design
    val username: String? = null, // Added to match WalletResponse
    val balance: BigDecimal = BigDecimal.ZERO,
    val lastUpdatedAt: String? = null, // ISO string for LocalDateTime
)
