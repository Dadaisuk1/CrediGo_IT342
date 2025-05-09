package com.sia.credigo.model

data class PurchaseRequest(
    val productId: Int,
    val quantity: Int = 1,
    val gameAccountId: String,
    val gameServerId: String? = null
)
