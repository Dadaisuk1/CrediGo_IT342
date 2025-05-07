package com.sia.credigo.network.models

data class LoginRequest(
    val email: String,
    val password: String
)


data class UserResponse(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phonenumber: String
)


