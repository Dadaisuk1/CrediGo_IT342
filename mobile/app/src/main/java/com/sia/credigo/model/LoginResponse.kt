package com.sia.credigo.model

data class LoginResponse(
    val message: String,
    val id: Int,
    val username: String,
    val email: String,
    val roles: Set<String>,
    val token: String
)
