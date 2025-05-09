package com.sia.credigo.network

import com.sia.credigo.model.WishlistItem
import retrofit2.Response
import retrofit2.http.*

interface WishlistApi {
    /**
     * Get current user's wishlist
     * Matches: WishlistController.getCurrentUserWishlist
     */
    @GET("api/wishlist")
    suspend fun getCurrentUserWishlist(): Response<List<WishlistItem>>

    /**
     * Add product to wishlist
     * Matches: WishlistController.addProductToWishlist
     */
    @POST("api/wishlist/{productId}")
    suspend fun addToWishlist(@Path("productId") productId: Int): Response<WishlistItem>

    /**
     * Remove product from wishlist
     * Matches: WishlistController.removeProductFromWishlist
     */
    @DELETE("api/wishlist/{productId}")
    suspend fun removeFromWishlist(@Path("productId") productId: Int): Response<Void>
}
