package com.sia.credigo.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.sia.credigo.model.LoginResponse
import com.sia.credigo.model.User
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson: Gson = GsonBuilder().setLenient().create()
    private val TAG = "SessionManager"

    companion object {
        private const val PREF_NAME = "CredigoSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val KEY_TOKEN_EXPIRY = "tokenExpiry"
        private const val KEY_USER_DATA = "userData"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_EMAIL = "userEmail"
    }

    fun saveLoginData(response: LoginResponse) {
        editor.putLong(KEY_USER_ID, response.id.toLong())
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USERNAME, response.username)
        editor.putString(KEY_USER_EMAIL, response.email)
        saveAuthToken(response.token)
        editor.apply()
        
        Log.d(TAG, "Saved login data for user ID: ${response.id}, username: ${response.username}")
    }

    fun saveUserData(user: User) {
        try {
            val userJson = gson.toJson(user)
            editor.putString(KEY_USER_DATA, userJson)
            editor.putLong(KEY_USER_ID, user.id.toLong())
            editor.putString(KEY_USERNAME, user.username)
            editor.putString(KEY_USER_EMAIL, user.email)
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.apply()
            
            Log.d(TAG, "Saved user data for ID: ${user.id}, username: ${user.username}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data: ${e.message}")
        }
    }

    fun getUserData(): User? {
        try {
            val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
            if (userJson != null) {
                try {
                    return gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user JSON: ${e.message}")
                    // If JSON parsing fails, try to create a basic user from the individual fields
                    return createBasicUserFromPrefs()
                }
            } else {
                // If no JSON data, try to create a basic user from the individual fields
                return createBasicUserFromPrefs()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data: ${e.message}")
            return null
        }
    }
    
    /**
     * Gets the user ID from the session
     */
    fun getUserId(): Int {
        return sharedPreferences.getLong(KEY_USER_ID, -1).toInt()
    }
    
    /**
     * Gets the username from the session
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    /**
     * Gets the user email from the session
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    private fun createBasicUserFromPrefs(): User? {
        try {
            val userId = sharedPreferences.getLong(KEY_USER_ID, -1)
            val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
            val email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
            
            if (userId != -1L && username.isNotEmpty() && email.isNotEmpty()) {
                Log.d(TAG, "Created basic user from prefs: ID=$userId, username=$username")
                // Create basic user with required fields
                return User(
                    id = userId.toInt(),
                    username = username,
                    email = email
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating basic user: ${e.message}")
        }
        return null
    }

    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        parseAndSaveTokenExpiry(token)
        editor.apply()
        Log.d(TAG, "Saved auth token")
    }

    fun saveRefreshToken(token: String) {
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
        Log.d(TAG, "Saved refresh token")
    }

    fun getAuthToken(): String? {
        return if (isTokenValid()) {
            sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        } else {
            Log.d(TAG, "Auth token expired or not found")
            null
        }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearAuthData() {
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_REFRESH_TOKEN)
        editor.remove(KEY_TOKEN_EXPIRY)
        editor.apply()
        Log.d(TAG, "Cleared auth data")
    }

    fun saveLoginState(userId: Long) {
        editor.putLong(KEY_USER_ID, userId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
        Log.d(TAG, "Saved login state for user ID: $userId")
    }

    fun clearLoginState() {
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USERNAME)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_USER_DATA)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        clearAuthData()
        editor.apply()
        Log.d(TAG, "Cleared login state")
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        Log.d(TAG, "isLoggedIn check: $loggedIn")
        return loggedIn
    }

    private fun parseAndSaveTokenExpiry(token: String) {
        try {
            // For mobile app, we'll use a long-lived token approach
            // Store an expiration date 30 days in the future
            val expiryTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)
            editor.putLong(KEY_TOKEN_EXPIRY, expiryTime)
            Log.d(TAG, "Set token expiry to 30 days from now: ${Date(expiryTime)}")
        } catch (e: Exception) {
            // Set a default expiry time (30 days from now) in case of parsing failure
            val expiryTime = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
            editor.putLong(KEY_TOKEN_EXPIRY, expiryTime)
            Log.e(TAG, "Error parsing token expiry, using default: ${Date(expiryTime)}")
        }
    }

    private fun isTokenValid(): Boolean {
        val expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        
        // For app usability, we'll consider a token valid if:
        // 1. We have a token at all
        // 2. Either the expiry time is in the future, OR
        // 3. The user is marked as logged in (session exists)
        val hasToken = !token.isNullOrEmpty()
        val inTime = expiryTime > System.currentTimeMillis()
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        
        val isValid = hasToken && (inTime || isLoggedIn)
        
        if (!isValid) {
            Log.d(TAG, "Token expired at: ${Date(expiryTime)}")
        }
        return isValid
    }
}
