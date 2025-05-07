package com.sia.credigo.network

import com.sia.credigo.model.Transaction
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {
    @POST("api/transactions")
    suspend fun createTransaction(@Body transaction: Transaction): Response<BaseResponse<Transaction>>

    @GET("api/transactions")
    suspend fun getTransactions(): Response<BaseResponse<List<Transaction>>>

    @GET("api/transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Long): Response<BaseResponse<Transaction>>
}
