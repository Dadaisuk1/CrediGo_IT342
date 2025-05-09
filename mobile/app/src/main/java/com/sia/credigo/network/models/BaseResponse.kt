package com.sia.credigo.network.models

/**
 * Standard wrapper for API responses.
 * This class matches the structure of responses from the Spring backend.
 */
data class BaseResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
) 