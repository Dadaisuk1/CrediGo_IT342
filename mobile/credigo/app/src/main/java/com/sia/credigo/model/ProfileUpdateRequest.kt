package com.sia.credigo.model

data class ProfileUpdateRequest(
    val username: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null
)
