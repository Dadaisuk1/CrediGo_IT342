package com.sia.credigo.network

import retrofit2.Call
import retrofit2.http.*

interface ReviewApi {
    @GET("products/{productId}/reviews")
    fun getProductReviews(@Path("productId") productId: Int): Call<List<Any>> // Replace Any with Review model

    @POST("products/{productId}/reviews")
    fun addReview(@Path("productId") productId: Int, @Body body: Any): Call<Any>

    @DELETE("products/{productId}/reviews")
    fun deleteReview(@Path("productId") productId: Int): Call<Void>

    @GET("reviews/user")
    fun getMyReviews(): Call<List<Any>>
}
