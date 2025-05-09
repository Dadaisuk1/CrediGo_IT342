package com.sia.credigo.network

import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionResponse
import com.sia.credigo.model.PurchaseRequest
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {
    /**
     * Create a purchase transaction
     * Matches the backend endpoint: TransactionController.initiatePurchase()
     */
    @POST("api/transactions/purchase")
    suspend fun purchaseProduct(@Body request: PurchaseRequest): Response<TransactionResponse>
    
    /**
     * Get transaction history for the authenticated user
     * Matches the backend endpoint: TransactionController.getMyTransactionHistory()
     */
    @GET("api/transactions/history")
    suspend fun getTransactionHistory(): Response<List<TransactionResponse>>
    
    /**
     * Legacy method - exists for backward compatibility
     * This endpoint doesn't match the backend exactly - used for internal app functionality
     */
    @POST("api/transactions")
    suspend fun createTransaction(@Body transaction: Transaction): Response<BaseResponse<Transaction>>

    /**
     * Legacy method - exists for backward compatibility
     */
    @GET("api/transactions")
    suspend fun getTransactions(): Response<BaseResponse<List<Transaction>>>

    /**
     * Legacy method - exists for backward compatibility
     */
    @GET("api/transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Long): Response<BaseResponse<Transaction>>
}
