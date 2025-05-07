package com.sia.credigo.model

data class PurchaseRequest(
    val productId: Int,
    val quantity: Int = 1,
    val gameAccountId: String? = null,
    val gameServerId: String? = null
)
