package com.sia.credigo.network

import com.sia.credigo.model.Wishlist
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*

interface WishlistApi {
    @GET("wishlist")
    suspend fun getAllWishlists(): Response<BaseResponse<List<Wishlist>>>

    @GET("wishlist/{id}")
    suspend fun getWishlistById(@Path("id") id: Long): Response<BaseResponse<Wishlist>>

    @POST("wishlist/{productId}")
    suspend fun addToWishlist(@Path("productId") productId: Long): Response<BaseResponse<Void>>

    @DELETE("wishlist/{productId}")
    suspend fun removeFromWishlist(@Path("productId") productId: Long): Response<BaseResponse<Void>>
}
