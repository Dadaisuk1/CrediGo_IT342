package com.sia.credigo.model

data class Platform(
    val id: Int,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val createdAt: Long = System.currentTimeMillis()  // Preferred for domain logic
)
