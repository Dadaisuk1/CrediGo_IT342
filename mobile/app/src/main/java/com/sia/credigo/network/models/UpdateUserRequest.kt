package com.sia.credigo.network.models

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    
    @SerializedName("phoneNumber")
    val phonenumber: String? = null
)
