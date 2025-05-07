package com.sia.credigo.network

import com.sia.credigo.model.User
import com.sia.credigo.model.UserResponse
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.*

interface UsersApi {
    @GET("users")
    suspend fun getAllUsers(): Response<BaseResponse<List<User>>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<BaseResponse<User>>

    // These endpoints match exactly with the AuthController paths
    @GET("auth/findByName/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<UserResponse>

    @GET("auth/findByEmail/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserResponse>

    @PUT("auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserResponse>
}
