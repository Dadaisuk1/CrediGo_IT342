package com.sia.credigo.model

/**
 * Data class for holding credit card details
 */
data class CardDetails(
    val cardNumber: String,
    val month: String,
    val year: String,
    val cvc: String,
    val cardHolderName: String
) 