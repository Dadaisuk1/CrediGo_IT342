package com.sia.credigo.network

import android.util.Log
import com.sia.credigo.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Extension functions for RetrofitClient (Deprecated)
 * This file is deprecated and maintained only for backward compatibility
 */
@Deprecated("Use RetrofitClient.initializeAuthenticatedRetrofit() directly")
object RetrofitClientExt {
    private const val TAG = "RetrofitClientExt"
    
    /**
     * Initialize RetrofitClient with the best available backend URL - DEPRECATED
     * Should be called during app startup
     */
    @Deprecated("No longer needed, RetrofitClient uses a fixed URL")
    suspend fun initBestBackendConnection(): Boolean {
        Log.w(TAG, "This method is deprecated and does nothing. RetrofitClient now uses a fixed URL.")
        return true
    }
    
    /**
     * Try to find a server that responds - DEPRECATED
     */
    @Deprecated("No longer needed, RetrofitClient uses a fixed URL")
    suspend fun findWorkingServer(): Boolean {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "This method is deprecated. Use RetrofitClient directly.")
            return@withContext true
        }
    }
}

// Define URL constants for reference only - not used anymore
const val EMULATOR_URL = "http://10.0.2.2:8080/"
const val LOCALHOST_URL = "http://192.168.1.5:8080/"
const val PRODUCTION_URL = "https://credigo-it342.onrender.com/"
