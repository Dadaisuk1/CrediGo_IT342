package com.sia.credigo.app

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ProcessLifecycleOwner
import com.sia.credigo.utils.SessionManager
import com.sia.credigo.network.models.UserResponse
import com.sia.credigo.network.RetrofitClient
import com.jakewharton.threetenabp.AndroidThreeTen
import com.sia.credigo.model.User

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
                sessionManager.saveLoginState(value.id.toLong())  // Use id instead of userid
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

        // Initialize login state
        isLoggedIn = sessionManager.isLoggedIn()

        // Initialize authenticated API services
        apiServices = RetrofitClient.createAuthenticatedServices(this, ProcessLifecycleOwner.get())

        AndroidThreeTen.init(this)
    }
}
