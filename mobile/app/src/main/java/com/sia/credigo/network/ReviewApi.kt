package com.sia.credigo.network

import com.sia.credigo.model.Review
import com.sia.credigo.network.models.BaseResponse
import com.sia.credigo.network.models.ReviewRequest
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @GET("products/{productId}/reviews")
    suspend fun getProductReviews(@Path("productId") productId: Int): Response<BaseResponse<List<Review>>>

    @POST("products/{productId}/reviews")
    suspend fun addReview(
        @Path("productId") productId: Int, 
        @Body reviewRequest: ReviewRequest
    ): Response<BaseResponse<Review>>

    @DELETE("products/{productId}/reviews")
    suspend fun deleteReview(@Path("productId") productId: Int): Response<BaseResponse<Void>>

    @GET("reviews/user")
    suspend fun getMyReviews(): Response<BaseResponse<List<Review>>>
}
