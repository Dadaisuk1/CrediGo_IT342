package com.sia.credigo.network

import com.sia.credigo.model.Product
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    /**
     * Get all products with optional platform filter
     * Matches: ProductController.getAllAvailableProducts
     */
    @GET("api/products")
    suspend fun getAllProducts(@Query("platformId") platformId: Int? = null): Response<List<Product>>

    /**
     * Get product by ID
     * Matches: ProductController.getProductById
     */
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<Product>

    /**
     * Create new product (admin only)
     * Matches: ProductController.createProduct
     */
    @POST("api/products/admin")
    suspend fun createProduct(@Body product: Product): Response<Product>

    /**
     * Update existing product (admin only)
     * Matches: ProductController.updateProduct
     */
    @PUT("api/products/admin/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body product: Product
    ): Response<Product>

    /**
     * Delete product (admin only)
     * Matches: ProductController.deleteProduct
     */
    @DELETE("api/products/admin/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Void>
}
