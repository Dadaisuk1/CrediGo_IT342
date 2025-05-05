package com.sia.credigo.network

import com.sia.credigo.model.LoginRequest
import com.sia.credigo.model.LoginResponse
import com.sia.credigo.network.models.RegisterRequest  // Changed from model.RegistrationRequest to network.models.RegisterRequest
import com.sia.credigo.model.ProfileUpdateRequest
import com.sia.credigo.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registrationRequest: RegisterRequest): Response<UserResponse>  // Changed class name to RegisterRequest
    
    @PUT("auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long, 
        @Body updateRequest: ProfileUpdateRequest
    ): Response<UserResponse>
}
