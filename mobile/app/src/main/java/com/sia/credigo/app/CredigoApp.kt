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
            }
        }

    // Token property for authentication
    var token: String? = null
        get() = sessionManager.getAuthToken()
        set(value) {
            field = value
            if (value != null) {
                sessionManager.saveAuthToken(value)
            } else {
                sessionManager.clearAuthData()
            }
        }

    // Database reference for activities that need it
    val database: Any? = null

    // Authenticated API services
    lateinit var apiServices: RetrofitClient.AuthenticatedServices
        private set

    companion object {
        lateinit var instance: CredigoApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)
        prefs = getSharedPreferences("credigo_prefs", MODE_PRIVATE)

        // DEVELOPMENT ONLY: Create test user if no session exists
        // Remove this in production!
        ensureTestUserExists()
        
        // Rest of code remains the same...
        val sessionActive = sessionManager.isLoggedIn()
        
        // Try multiple sources to determine if we're logged in
        isLoggedIn = sessionActive
        
        // First try to get the user data from session
        if (isLoggedIn || sessionManager.getUserId() > 0) {
            loggedInuser = sessionManager.getUserData()
            
            // If user data was loaded successfully, make sure login state is consistent
            if (loggedInuser != null) {
                isLoggedIn = true
                sessionManager.saveLoginState(loggedInuser!!.id.toLong())
                Log.d("CredigoApp", "Session restored with user: ${loggedInuser!!.username}, ID: ${loggedInuser!!.id}")
            } else if (sessionActive) {
                // If session reports active but we couldn't get user data, try to create minimal user
                val userId = sessionManager.getUserId()
                val username = sessionManager.getUsername()
                val email = sessionManager.getUserEmail()
                
                if (userId > 0 && username != null) {
                    loggedInuser = User(
                        id = userId,
                        username = username,
                        email = email ?: "user@example.com"
                    )
                    isLoggedIn = true
                    Log.d("CredigoApp", "Created minimal user from session: $username, ID: $userId")
                } else {
                    isLoggedIn = false
                    sessionManager.clearLoginState()
                    Log.d("CredigoApp", "Session logged in but missing user data, clearing session")
                }
            }
        } else {
            isLoggedIn = false
            Log.d("CredigoApp", "No active session found")
        }

        // Initialize authenticated API services
        apiServices = RetrofitClient.createAuthenticatedServices(this, ProcessLifecycleOwner.get())

        AndroidThreeTen.init(this)
    }
    
    /**
     * DEVELOPMENT ONLY - Creates a test user if no user exists
     * This ensures the app always has a logged in user for testing
     * Remove this method in production!
     */
    private fun ensureTestUserExists() {
        // Check if we already have a user
        if (sessionManager.isLoggedIn() && sessionManager.getUserData() != null) {
            Log.d("CredigoApp", "Test user already exists, using existing session")
            return
        }
        
        // Create a test user for development purposes
        Log.d("CredigoApp", "Creating test user for development")
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )
        
        // Save test user to session
        sessionManager.saveUserData(testUser)
        sessionManager.saveLoginState(1)
        
        // Create a dummy auth token
        sessionManager.saveAuthToken("test_token_for_development")
        
        // Update app state
        loggedInuser = testUser
        isLoggedIn = true
    }
}
