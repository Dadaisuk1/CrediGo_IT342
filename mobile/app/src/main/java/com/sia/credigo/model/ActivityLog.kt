package com.sia.credigo.model

import java.time.LocalDateTime

/**
 * Internal representation of activity logs for the app
 */
data class ActivityLog(
    val id: Long,
    val userId: Int,
    val username: String,
    val activity: String,
    val activityType: String,
    val details: String?,
    val ipAddress: String?,
    val timestamp: String // LocalDateTime as string
) {
    companion object {
        // Convert from API response to internal model
        fun fromResponse(response: ActivityLogResponse): ActivityLog {
            return ActivityLog(
                id = response.id,
                userId = response.userId,
                username = response.username,
                activity = response.activity,
                activityType = response.activityType,
                details = response.details,
                ipAddress = response.ipAddress,
                timestamp = response.timestamp
            )
        }
    }
} 