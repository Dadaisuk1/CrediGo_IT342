package com.sia.credigo.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("api/health")
    fun checkHealth(): Call<String>
}
