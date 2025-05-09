package com.sia.credigo.model

data class Platform(
    val id: Int,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val createdAt: String? = null  // Changed from Long to String? to match server response
)
