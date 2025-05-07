package com.sia.credigo.network

import com.sia.credigo.model.Wallet
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.UpdateWalletRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface WalletApi {
    @GET("wallet/me")
    suspend fun getMyWallet(): Response<BaseResponse<Wallet>>

    @POST("wallet/create-payment-intent")
    suspend fun createWalletTopUpIntent(@Body topUpRequest: Map<String, Any>): Response<BaseResponse<Map<String, String>>>

    @GET("wallets")
    suspend fun getAllWallets(): Response<BaseResponse<List<Wallet>>>

    @GET("wallets/{id}")
    suspend fun getWalletById(@Path("id") walletId: Long): Response<BaseResponse<Wallet>>

    @GET("wallets/user/{userId}")
    suspend fun getWalletByUserId(@Path("userId") userId: Long): Response<BaseResponse<Wallet>>

    @PUT("wallets/{id}")
    suspend fun updateWallet(
        @Path("id") walletId: Long,
        @Body request: UpdateWalletRequest
    ): Response<BaseResponse<Wallet>>
}
