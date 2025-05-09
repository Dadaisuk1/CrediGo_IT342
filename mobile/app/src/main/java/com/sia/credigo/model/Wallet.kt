package com.sia.credigo.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data class representing a user's wallet.
 * Matches the backend Wallet entity structure.
 */
data class Wallet(
    val id: Int = 0,
    val userId: Int? = null, // Use Int? for flexibility, or User if backend returns full user object
    val balance: BigDecimal = BigDecimal.ZERO,
    val lastUpdatedAt: String? = null, // ISO string for LocalDateTime
)
