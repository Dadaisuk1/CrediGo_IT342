package com.sia.credigo.network

import com.sia.credigo.model.PaymentResponse
import com.sia.credigo.model.Wallet
import com.sia.credigo.model.WalletTopUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit service interface for wallet-related API endpoints
 */
interface WalletService {
    
    /**
     * Get the authenticated user's wallet
     */
    @GET("api/wallet/my")
    suspend fun getMyWallet(): Response<Wallet>
    
    /**
     * Create a wallet top-up payment intent through PayMongo
     */
    @POST("api/wallet/topup/intent")
    suspend fun createWalletTopUpIntent(@Body request: WalletTopUpRequest): Response<Map<String, String>>
    
    /**
     * Direct top-up wallet endpoint
     */
    @POST("api/wallet/topup")
    suspend fun topupWallet(@Body request: WalletTopUpRequest): Response<Wallet>
    
    /**
     * Create a payment using credit card details
     */
    @POST("api/payments/card")
    suspend fun createCardPayment(@Body request: Map<String, Any>): Response<PaymentResponse>
    
    /**
     * Create a payment using e-wallet (GCash/Maya)
     */
    @POST("api/payments/ewallet")
    suspend fun createEWalletPayment(@Body request: Map<String, Any>): Response<PaymentResponse>
} 