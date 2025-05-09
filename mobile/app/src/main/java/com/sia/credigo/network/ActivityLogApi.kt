package com.sia.credigo.network

import com.sia.credigo.model.ActivityLogResponse
import com.sia.credigo.model.PagedResponse  // Add this import
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ActivityLogApi {
    
    @GET("activity-logs/user/{userId}")
    suspend fun getUserActivityLogs(
        @Path("userId") userId: Int
    ): Response<List<ActivityLogResponse>>
    
    @GET("activity-logs/user/{userId}/paged")
    suspend fun getUserActivityLogsPaged(
        @Path("userId") userId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<PagedResponse<ActivityLogResponse>>
    
    @GET("activity-logs/{logId}")
    suspend fun getActivityLogById(
        @Path("logId") logId: Long
    ): Response<ActivityLogResponse>
}
