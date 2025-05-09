package com.sia.credigo.model

import java.math.BigDecimal

data class Product(
    val productid: Int = 0,  // matches @Id in backend
    val platformid: Int = 0,  // matches platform.id in backend
    val name: String = "",
    val description: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,  // matches backend precision
    val itemCode: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val createdAt: String? = null  // ISO format string for LocalDateTime
)