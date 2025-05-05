package com.sia.credigo.model

import java.math.BigDecimal

data class User(
    val id: Int,           // Primary ID field matching backend
    val userid: Int = id,  // Alias for backward compatibility
    val username: String,
    val email: String,
    val phonenumber: String? = null,
    val dateOfBirth: String? = null,
    val createdAt: String? = null,
    val roles: Set<String> = emptySet(),
    val active: Boolean = true,
    val balance: BigDecimal? = null
)
