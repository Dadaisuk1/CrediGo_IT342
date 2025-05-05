package com.sia.credigo.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sia.credigo.model.LoginResponse

class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "credigo_session"
        private const val AUTH_TOKEN = "auth_token"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_EMAIL = "user_email"
        private const val USER_ROLES = "user_roles"
    }
    
    private val sharedPreferences: SharedPreferences
    
    init {
        // Create or retrieve the Master Key for encryption/decryption
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        // Create the encrypted SharedPreferences
        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    fun saveLoginData(loginResponse: LoginResponse) {
        sharedPreferences.edit().apply {
            putString(AUTH_TOKEN, loginResponse.token)
            putInt(USER_ID, loginResponse.id)
            putString(USER_NAME, loginResponse.username)
            putString(USER_EMAIL, loginResponse.email)
            putStringSet(USER_ROLES, loginResponse.roles)
            apply()
        }
    }
    
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN, null)
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(USER_ID, -1)
    }
    
    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME, null)
    }
    
    fun getUserEmail(): String? {
        return sharedPreferences.getString(USER_EMAIL, null)
    }
    
    fun getUserRoles(): Set<String>? {
        return sharedPreferences.getStringSet(USER_ROLES, null)
    }
    
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun hasRole(role: String): Boolean {
        val roles = getUserRoles() ?: return false
        return roles.contains(role)
    }
    
    fun isAdmin(): Boolean {
        return hasRole("ROLE_ADMIN")
    }
}
