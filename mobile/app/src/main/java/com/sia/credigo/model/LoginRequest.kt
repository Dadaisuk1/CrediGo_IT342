package com.sia.credigo.model

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)
