package com.sia.credigo.network

import android.util.Log
import com.sia.credigo.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds authorization header to requests
 * that require authentication using JWT tokens stored in SessionManager.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()
        
        // Don't add auth header for login/register requests
        if (isAuthEndpoint(requestUrl)) {
            Log.d(TAG, "Skipping auth for auth endpoint: $requestUrl")
            return chain.proceed(originalRequest)
        }
        
        val token = sessionManager.getAuthToken()
        
        // Log token status (debug only - remove in production)
        if (token == null) {
            Log.e(TAG, "No auth token found for request to: $requestUrl")
            
            // Check login state - if user should be logged in but has no token, log the inconsistency
            if (sessionManager.isLoggedIn()) {
                Log.e(TAG, "User is logged in but has no token - session inconsistency detected")
            }
        } else {
            Log.d(TAG, "Adding auth token to request: $requestUrl")
        }

        // Build new request with auth header if token is available
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            // Proceed with original request if no token (will likely fail with 401/403)
            Log.w(TAG, "Proceeding without auth token for: $requestUrl - request will likely fail")
            originalRequest
        }

        val response = chain.proceed(newRequest)
        
        // Log authentication failures
        if (response.code == 401 || response.code == 403) {
            Log.e(TAG, "Authentication failure (${response.code}) for: $requestUrl")
            
            if (token != null) {
                Log.d(TAG, "Token used: ${token.take(15)}...")
            }
        }
        
        return response
    }

    private fun isAuthEndpoint(url: String): Boolean {
        return url.contains("api/auth/login") || 
               url.contains("api/auth/register") ||
               url.contains("api/auth/refresh")
    }
}
