package com.sia.credigo.network.models

data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    val phonenumber: String? = null
)
