package com.sia.credigo.model

import java.math.BigDecimal

// Matches backend Wallet entity and best practices for DTOs

data class Wallet(
    val id: Int = 0,
    val userId: Int? = null, // Use Int? for flexibility, or User if backend returns full user object
    val balance: Double = 0.0,
    val lastUpdatedAt: String? = null, // ISO string, nullable for flexibility
)
