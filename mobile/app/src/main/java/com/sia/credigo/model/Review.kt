package com.sia.credigo.model

data class Review(
    val userId: Int,
    val username: String,
    val productId: Int,
    val rating: Int,
    val comment: String?,
    val reviewTimestamp: String
)

