package com.sia.credigo.network.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,  // Match the backend's UserRegistrationRequest 'username' field
    val phonenumber: String  // Use camelCase as this is what Spring Boot typically expects
)
