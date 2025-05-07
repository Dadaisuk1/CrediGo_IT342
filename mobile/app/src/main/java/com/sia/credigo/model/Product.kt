package com.sia.credigo.model

data class Product(
    val productid: Long = 0,
    val platformid: Long = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val itemCode: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val createdAt: String? = null  // ISO format string
)