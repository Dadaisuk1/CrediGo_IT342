package com.sia.credigo.network

import com.sia.credigo.model.Platform
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*


interface PlatformApi {
    @GET("api/platforms")
    suspend fun getAllPlatforms(): Response<List<Platform>>

    @GET("api/platforms/{id}")
    suspend fun getPlatformById(@Path("id") id: Long): Response<Platform>

    @POST("api/platforms/admin")
    suspend fun createPlatform(@Body body: Platform): Response<Platform>

    @PUT("api/platforms/admin/{id}")
    suspend fun updatePlatform(@Path("id") id: Long, @Body body: Platform): Response<Platform>

    @DELETE("api/platforms/admin/{id}")
    suspend fun deletePlatform(@Path("id") id: Long): Response<Void>
}
