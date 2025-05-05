package com.sia.credigo.repository

import com.sia.credigo.model.ActivityLogResponse
import com.sia.credigo.model.PagedResponse
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.util.NetworkResult

class ActivityLogRepository {
    
    private val activityLogApi = RetrofitClient.activityLogService
    
    suspend fun getUserActivityLogs(userId: Int): NetworkResult<List<ActivityLogResponse>> {
        return try {
            val response = activityLogApi.getUserActivityLogs(userId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getUserActivityLogsPaged(
        userId: Int,
        page: Int,
        size: Int
    ): NetworkResult<PagedResponse<ActivityLogResponse>> {
        return try {
            val response = activityLogApi.getUserActivityLogsPaged(userId, page, size)
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
            val response = activityLogApi.getActivityLogById(logId)
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
