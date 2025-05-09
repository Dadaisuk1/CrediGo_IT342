package com.sia.credigo.model

import java.math.BigDecimal

/**
 * Represents a wishlist item in the user's wishlist.
 * In the backend, this uses a composite key of (userId, productId).
 */
data class WishlistItem(
    // Composite key fields
    val userId: Int,
    val productId: Int,

    // Product details
    val productName: String? = null,
    val productPrice: BigDecimal? = null,
    val productDescription: String? = null,
    val productImageUrl: String? = null,
    val productIsAvailable: Boolean = true,

    // Platform details
    val platformId: Int? = null,
    val platformName: String? = null,

    // Metadata
    val addedAt: String? = null  // ISO format string for LocalDateTime
) {
    /**
     * Helper method to check if this wishlist item belongs to a specific user
     */
    fun belongsToUser(checkUserId: Int): Boolean = userId == checkUserId

    /**
     * Helper method to check if this wishlist item is for a specific product
     */
    fun isForProduct(checkProductId: Int): Boolean = productId == checkProductId

    companion object {
        /**
         * Creates a minimal WishlistItem for adding to wishlist
         */
        fun createForAdd(userId: Int, productId: Int): WishlistItem {
            return WishlistItem(
                userId = userId,
                productId = productId
            )
        }
    }
} 