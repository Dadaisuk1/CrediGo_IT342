package com.sia.credigo.network.models

/**
 * Request model for user registration
 * Fields match exactly what the backend expects in UserRegistrationRequest
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null
)
