package com.sia.credigo.repository

import android.util.Log
import com.sia.credigo.model.ActivityLog
import com.sia.credigo.model.ActivityLogResponse
import com.sia.credigo.model.PagedResponse
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.util.NetworkResult

class ActivityLogRepository {
    private val TAG = "ActivityLogRepository"
    
    // Use the service directly from RetrofitClient
    private val activityLogService = RetrofitClient.activityLogService
    
    suspend fun getUserActivityLogs(userId: Int): List<ActivityLog> {
        try {
            val response = activityLogService.getUserActivityLogs(userId)
            if (response.isSuccessful) {
                return response.body()?.map { ActivityLog.fromResponse(it) } ?: emptyList()
            } else {
                Log.e(TAG, "Error fetching activity logs: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching activity logs: ${e.message}", e)
        }
        return emptyList()
    }
    
    suspend fun getUserActivityLogsResponse(userId: Int): NetworkResult<List<ActivityLogResponse>> {
        return try {
            val response = activityLogService.getUserActivityLogs(userId)
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: emptyList())
            } else {
                NetworkResult.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getUserActivityLogsPaged(
        userId: Int,
        page: Int,
        size: Int
    ): NetworkResult<PagedResponse<ActivityLogResponse>> {
        return try {
            val response = activityLogService.getUserActivityLogsPaged(userId, page, size)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getActivityLogById(logId: Long): NetworkResult<ActivityLogResponse> {
        return try {
            val response = activityLogService.getActivityLogById(logId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }
}
