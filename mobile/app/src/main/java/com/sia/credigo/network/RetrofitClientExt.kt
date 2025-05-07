package com.sia.credigo.network

import android.util.Log
import com.sia.credigo.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extension functions for RetrofitClient
 */
object RetrofitClientExt {
    private const val TAG = "RetrofitClientExt"
    
    /**
     * Initialize RetrofitClient with the best available backend URL
     * Should be called during app startup
     */
    suspend fun initBestBackendConnection(): Boolean {
        return try {
            val workingUrl = NetworkUtils.findWorkingBackendUrl()
            if (workingUrl != null) {
                Log.d(TAG, "Setting working backend URL: $workingUrl")
                RetrofitClient.BASE_URL = workingUrl
                true
            } else {
                Log.w(TAG, "No working backend found, using default URL")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing backend connection", e)
            false
        }
    }
}

// Add these URL constants to RetrofitClient
const val EMULATOR_URL = "http://10.0.2.2:8080/"
const val LOCALHOST_URL = "http://192.168.1.5:8080/"
const val PRODUCTION_URL = "https://credigo-it342.onrender.com/"
