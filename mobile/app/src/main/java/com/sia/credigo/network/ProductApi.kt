package com.sia.credigo.network

import com.sia.credigo.model.Product
import com.sia.credigo.network.models.BaseResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getAllProducts(): Response<BaseResponse<List<Product>>>
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<BaseResponse<Product>>

    @POST("products/admin")
    suspend fun createProduct(@Body product: Product): Response<BaseResponse<Product>>
    @PUT("products/admin/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body product: Product
    ): Response<BaseResponse<Product>>

    @DELETE("products/admin/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<BaseResponse<Void>>
}
