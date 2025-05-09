package com.sia.credigo.network

import com.sia.credigo.model.Platform
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*


interface PlatformApi {
    /**
     * Get all platforms
     * Matches: PlatformController.getAllPlatforms
     */
    @GET("api/platforms")
    suspend fun getAllPlatforms(): Response<List<Platform>>

    /**
     * Get platform by ID
     * Matches: PlatformController.getPlatformById
     */
    @GET("api/platforms/{id}")
    suspend fun getPlatformById(@Path("id") id: Int): Response<Platform>

    /**
     * Create new platform (admin only)
     * Matches: PlatformController.createPlatform
     */
    @POST("api/platforms/admin")
    suspend fun createPlatform(@Body platform: Platform): Response<Platform>

    /**
     * Update existing platform (admin only)
     * Matches: PlatformController.updatePlatform
     */
    @PUT("api/platforms/admin/{id}")
    suspend fun updatePlatform(
        @Path("id") id: Int,
        @Body platform: Platform
    ): Response<Platform>

    @DELETE("api/platforms/admin/{id}")
    suspend fun deletePlatform(@Path("id") id: Long): Response<Void>
}
