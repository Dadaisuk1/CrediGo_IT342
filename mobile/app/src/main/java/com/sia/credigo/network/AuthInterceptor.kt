package com.sia.credigo.network

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.sia.credigo.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null
) : Interceptor {

    private val sessionManager by lazy { SessionManager(context) }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip interceptor for login and registration endpoints
        if (originalRequest.url.encodedPath.endsWith("login") || 
            originalRequest.url.encodedPath.endsWith("register")) {
            return chain.proceed(originalRequest)
        }
        
        val authToken = runBlocking { sessionManager.getAuthToken() }
        
        return if (authToken != null) {
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
            
            val response = chain.proceed(requestWithAuth)
            
            // Handle 401/403 by refreshing token or redirecting to login
            if (response.code == 401 || response.code == 403) {
                // Token expired or invalid
                runBlocking { 
                    sessionManager.clearLoginState() 
                }
                response
            } else {
                response
            }
        } else {
            // No token available, proceed without Authorization header
            chain.proceed(originalRequest)
        }
    }
}
