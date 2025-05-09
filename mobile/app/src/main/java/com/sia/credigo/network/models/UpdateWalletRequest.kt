package com.sia.credigo.network.models

data class UpdateWalletRequest(
    val balance: Double? = null,
    val userId: Int? = null
)