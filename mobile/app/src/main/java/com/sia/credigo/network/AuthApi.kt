package com.sia.credigo.network

import com.sia.credigo.model.LoginRequest
import com.sia.credigo.model.LoginResponse
import com.sia.credigo.model.UserResponse
import com.sia.credigo.model.ProfileUpdateRequest
import com.sia.credigo.network.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.Headers

interface AuthApi {
    // Fix endpoints to ensure they match backend exactly
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body registrationRequest: RegisterRequest): Response<UserResponse>

    @PUT("api/auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long, 
        @Body updateRequest: ProfileUpdateRequest
    ): Response<UserResponse>
    
    @GET("api/auth/findByEmail/{email}")
    suspend fun findByEmail(@Path("email") email: String): Response<UserResponse>
    
    @GET("api/auth/findByName/{username}")
    suspend fun findByUsername(@Path("username") username: String): Response<UserResponse>
}
