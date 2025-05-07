package com.sia.credigo.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Payment endpoints
interface PaymentApi {
    @POST("payments/create-payment-intent")
    fun createPaymentIntent(@Body body: Any): Call<Any> // Replace 'Any' with your request/response
}