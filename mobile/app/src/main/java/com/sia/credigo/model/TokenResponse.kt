package com.sia.credigo.model

/**
 * Response class for token refresh operations
 */
data class TokenResponse(
    val token: String,
    val expiresIn: Long = 0,
    val tokenType: String = "Bearer"
) 