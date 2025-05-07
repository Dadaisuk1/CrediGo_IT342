package com.sia.credigo.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ActivityLogResponse(
    val id: Long,
    val userId: Int,
    val username: String,
    val activity: String,
    val activityType: String,
    val details: String?,
    val ipAddress: String?,
    @SerializedName("createdAt")
    val timestamp: String // LocalDateTime as string
)
