package com.sia.credigo.model

import java.math.BigDecimal

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val createdAt: String,
    val roles: Set<String>,
    val active: Boolean?,
    val balance: BigDecimal?
)
