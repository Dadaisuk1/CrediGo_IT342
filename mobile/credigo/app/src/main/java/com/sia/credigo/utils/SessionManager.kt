package com.sia.credigo.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.sia.credigo.network.models.LoginResponse
import java.util.*

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "CredigoSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val KEY_TOKEN_EXPIRY = "tokenExpiry"
    }

    fun saveLoginData(response: LoginResponse) {
        editor.putLong(KEY_USER_ID, response.data.userId.toLong())
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        saveAuthToken(response.data.accessToken)
        saveRefreshToken(response.data.refreshToken)
        editor.apply()
    }

    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        parseAndSaveTokenExpiry(token)
        editor.apply()
    }

    fun saveRefreshToken(token: String) {
        editor.putString(KEY_REFRESH_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return if (isTokenValid()) {
            sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        } else {
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
    }

    fun saveLoginState(userId: Long) {
        editor.putLong(KEY_USER_ID, userId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun clearLoginState() {
        editor.remove(KEY_USER_ID)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun parseAndSaveTokenExpiry(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charsets.UTF_8)
                val json = payload // Always use payload for parsing
                val expirySeconds = json.substringAfter("\"exp\":").substringBefore("}").toLong()
                editor.putLong(KEY_TOKEN_EXPIRY, expirySeconds * 1000)
            }
        } catch (e: Exception) {
            // Set a default expiry time (1 hour from now) in case of parsing failure
            editor.putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + 3600000)
        }
    }

    private fun isTokenValid(): Boolean {
        val expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return expiryTime > System.currentTimeMillis()
    }
}
