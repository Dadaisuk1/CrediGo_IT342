package com.sia.credigo.network

import com.sia.credigo.model.User
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.RegisterRequest
import com.sia.credigo.network.models.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.*

interface UsersApi {
    @GET("users")
    suspend fun getAllUsers(): Response<BaseResponse<List<User>>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<BaseResponse<User>>

    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<BaseResponse<User>>

    @GET("users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<BaseResponse<User>>

    @POST("users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<BaseResponse<User>>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UpdateUserRequest
    ): Response<BaseResponse<User>>
}
