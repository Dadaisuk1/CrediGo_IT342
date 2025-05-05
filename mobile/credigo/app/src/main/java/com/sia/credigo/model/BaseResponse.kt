package com.sia.credigo.network.models

data class BaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
