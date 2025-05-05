package com.sia.credigo.network

import com.sia.credigo.model.Platform
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*


interface PlatformApi {
    @GET("platforms")
    suspend fun getAllPlatforms(): Response<BaseResponse<List<Platform>>>

    @GET("platforms/{id}")
    suspend fun getPlatformById(@Path("id") id: Long): Response<BaseResponse<Platform>>

    @POST("platforms/admin")
    suspend fun createPlatform(@Body body: Platform): Response<BaseResponse<Platform>>

    @PUT("platforms/admin/{id}")
    suspend fun updatePlatform(@Path("id") id: Long, @Body body: Platform): Response<BaseResponse<Platform>>

    @DELETE("platforms/admin/{id}")
    suspend fun deletePlatform(@Path("id") id: Long): Response<BaseResponse<Void>>
}
