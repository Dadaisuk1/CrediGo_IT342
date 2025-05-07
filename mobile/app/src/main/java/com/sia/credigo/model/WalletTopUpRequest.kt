package com.sia.credigo.model

import java.math.BigDecimal

data class WalletTopUpRequest(
    val amount: BigDecimal,
    val paymentMethod: String? = "paymongo"
)
