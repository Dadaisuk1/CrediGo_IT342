package com.sia.credigo.model

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    val username: String? = null,
    val password: String? = null,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)
