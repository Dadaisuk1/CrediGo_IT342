package com.sia.credigo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.sia.credigo.network.RetrofitClient

object NetworkUtils {
    private const val TAG = "NetworkUtils"
    
    // Define our server URLs directly
    private const val EMULATOR_SERVER = "http://10.0.2.2:8080/api/auth/test"
    private const val LOCALHOST_SERVER = "http://192.168.1.5:8080/api/auth/test"
    private const val PRODUCTION_SERVER = "https://credigo-it342.onrender.com/api/auth/test"
    
    /**
     * Check if the device has network connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Check if the backend server is reachable by trying different URLs
     */
    suspend fun findWorkingBackendUrl(): String? = withContext(Dispatchers.IO) {
        val urls = listOf(
            EMULATOR_SERVER,
            PRODUCTION_SERVER,
            LOCALHOST_SERVER
        )
        
        for (url in urls) {
            try {
                Log.d(TAG, "Checking server connectivity: $url")
                val isReachable = withTimeoutOrNull(5000L) {
                    isServerReachable(url)
                } ?: false
                
                if (isReachable) {
                    Log.d(TAG, "Found working server: $url")
                    return@withContext url.substringBefore("/api/auth/test")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking server: $url", e)
            }
        }
        
        Log.w(TAG, "No working backend server found")
        return@withContext null
    }
    
    /**
     * Check if a specific server URL is reachable
     */
    private suspend fun isServerReachable(serverUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Server response code: $responseCode for $serverUrl")
            
            // Consider 2xx, 3xx, 401, 403 and 404 as "reachable" since they indicate
            // the server is up but might be restricting access
            (responseCode in 200..399) || responseCode == 401 || responseCode == 403 || responseCode == 404
        } catch (e: Exception) {
            Log.e(TAG, "Server unreachable: $serverUrl", e)
            false
        }
    }
}
