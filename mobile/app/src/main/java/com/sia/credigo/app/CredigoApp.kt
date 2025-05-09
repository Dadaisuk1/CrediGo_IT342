package com.sia.credigo.app

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ProcessLifecycleOwner
import com.sia.credigo.utils.SessionManager
import com.sia.credigo.network.models.UserResponse
import com.sia.credigo.network.RetrofitClient
import com.jakewharton.threetenabp.AndroidThreeTen
import com.sia.credigo.model.User
import android.util.Log
import com.sia.credigo.manager.WalletManager

class CredigoApp : Application() {
    lateinit var sessionManager: SessionManager
    private lateinit var prefs: SharedPreferences

    var loggedInUser: UserResponse? = null
        set(value) {
            field = value
            if (value != null) {
                sessionManager.saveLoginState(value.id.toLong())
            } else {
                sessionManager.clearLoginState()
            }
        }

    // Added for backward compatibility with existing code
    var loggedInuser: User? = null
        set(value) {
            field = value
            if (value != null) {
                sessionManager.saveLoginState(value.id.toLong())
                sessionManager.saveUserData(value)  // Save full user data in session
                
                // Initialize services before wallet manager
                RetrofitClient.initializeAuthenticatedRetrofit(sessionManager)
                
                // Now initialize wallet when user is set
                WalletManager.initialize(value.id)
            } else {
                sessionManager.clearLoginState()
            }
        }

    var isLoggedIn: Boolean = false
        get() = loggedInUser != null || loggedInuser != null
        set(value) {
            field = value
            if (!value) {
                loggedInUser = null
                loggedInuser = null
                sessionManager.clearLoginState()
                
                // Reset wallet manager on logout
                WalletManager.reset()
            }
        }

    // Token property for authentication
    var token: String? = null
        get() = sessionManager.getAuthToken()
        set(value) {
            field = value
            if (value != null) {
                sessionManager.saveAuthToken(value)
                // Reinitialize authenticated services when token changes
                RetrofitClient.initializeAuthenticatedRetrofit(sessionManager)
            } else {
                sessionManager.clearAuthData()
            }
        }

    // Database reference for activities that need it
    val database: Any? = null

    companion object {
        lateinit var instance: CredigoApp
            private set
    }

    private val TAG = "CredigoApp"

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)
        prefs = getSharedPreferences("credigo_prefs", MODE_PRIVATE)
        
        Log.d(TAG, "Initializing CredigoApp")
        
        // Check if user is already logged in from session
        val sessionActive = sessionManager.isLoggedIn()
        isLoggedIn = sessionActive
        
        if (sessionActive) {
            // Verify token exists
            val token = sessionManager.getAuthToken()
            if (token != null) {
                Log.d(TAG, "Valid token found in session")
                
                // Initialize API with the session token FIRST
                RetrofitClient.initializeAuthenticatedRetrofit(sessionManager)
                
                // Load user data from session
                loggedInuser = sessionManager.getUserData()
                
                // If user data was successfully loaded, initialize wallet manager
                if (loggedInuser != null) {
                    Log.d(TAG, "Session restored with user: ${loggedInuser!!.username}, ID: ${loggedInuser!!.id}")
                    
                    // Initialize wallet manager with the logged-in user
                    WalletManager.initialize(loggedInuser!!.id)
                } else {
                    // Session claims to be active but no user data found - invalid state
                    Log.w(TAG, "Session reports active but no user data found")
                    isLoggedIn = false
                    sessionManager.clearLoginState()
                }
            } else {
                Log.w(TAG, "Session active but no valid token found")
                isLoggedIn = false
                sessionManager.clearLoginState()
            }
        } else {
            Log.d(TAG, "No active session found")
            isLoggedIn = false
        }

        AndroidThreeTen.init(this)
    }
    
    /**
     * Set the logged in user and initialize all services
     */
    fun setLoggedInUser(user: User) {
        Log.d(TAG, "Setting logged in user: ${user.id}")
        
        // Update app state
        loggedInuser = user
        isLoggedIn = true
        
        // Save to session
        sessionManager.saveUserData(user)
        
        // Initialize API services FIRST
        RetrofitClient.initializeAuthenticatedRetrofit(sessionManager)
        
        // Initialize wallet AFTER services are initialized
        WalletManager.initialize(user.id)
    }
    
    /**
     * Perform logout and cleanup all services
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        
        // Reset wallet manager
        WalletManager.reset()
        
        // Reset API services
        RetrofitClient.resetAuthenticatedRetrofit()
        
        // Clear session
        sessionManager.clearLoginState()
        
        // Clear state
        loggedInuser = null
        isLoggedIn = false
    }
}
