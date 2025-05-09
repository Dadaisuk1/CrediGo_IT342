package com.sia.credigo.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a response from the PayMongo payment API
 */
data class PaymentResponse(
    // PayMongo checkout URL
    @SerializedName("checkout_url")
    val checkoutUrl: String? = null,
    
    // PayMongo client secret for direct checkout
    @SerializedName("client_secret")
    val clientSecret: String? = null,
    
    // Success or error message
    @SerializedName("message")
    val message: String? = null,
    
    // Payment ID from PayMongo
    @SerializedName("id")
    val id: String? = null,
    
    // Payment status
    @SerializedName("status")
    val status: String? = null,
    
    // Error code if any
    @SerializedName("error")
    val error: PaymentError? = null
)

/**
 * Represents an error response from PayMongo
 */
data class PaymentError(
    @SerializedName("code")
    val code: String? = null,
    
    @SerializedName("message")
    val message: String? = null
) 