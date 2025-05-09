package com.sia.credigo.network

import com.sia.credigo.model.Wallet
import com.sia.credigo.model.WalletTopUpRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for wallet-related operations.
 * Aligns with backend endpoints at /api/wallet/
 */
interface WalletApi {
    /**
     * Get the authenticated user's wallet
     * Matches: WalletController.getCurrentUserWallet()
     */
    @GET("api/wallet/me")
    suspend fun getMyWallet(): Response<Wallet>
    
    /**
     * Create a payment intent for wallet top-up
     * Matches: WalletController.createWalletTopUpIntent()
     * 
     * Note: This endpoint not only creates a payment intent but also handles wallet top-up
     * on the backend side. The wallet balance is updated when this endpoint is called.
     */
    @POST("api/wallet/create-payment-intent")
    suspend fun createWalletTopUpIntent(@Body topUpRequest: WalletTopUpRequest): Response<Map<String, String>>
}
