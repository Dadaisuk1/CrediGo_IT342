package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.model.User
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.*

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var currentUser: User
    private var currentWallet: Wallet? = null

    // UI components
    private lateinit var usernameTextView: TextView
    private lateinit var balanceTextView: TextView
    private lateinit var emailTextView: TextView

    // Menu items
    private lateinit var walletMenu: LinearLayout
    private lateinit var mailsMenu: LinearLayout
    private lateinit var transactionsMenu: LinearLayout
    private lateinit var wishlistMenu: LinearLayout
    private lateinit var searchMenu: LinearLayout
    private lateinit var settingsMenu: LinearLayout
    private lateinit var logoutMenu: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate started")

        // Initialize ViewModels
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Get current user from application
        val app = application as CredigoApp
        Log.d(TAG, "App instance retrieved, loggedInUser: ${app.loggedInuser != null}, isLoggedIn: ${app.sessionManager.isLoggedIn()}")
        
        // Try multiple approaches to get the user data to avoid unnecessary logout
        var userData: User? = app.loggedInuser
        
        Log.d(TAG, "Initial user data check: ${userData != null}")
        Log.d(TAG, "Session logged in: ${app.sessionManager.isLoggedIn()}")
        
        // If app doesn't have the user, check if we're still logged in and try to get from session
        if (userData == null) {
            Log.d(TAG, "User data not found in app, checking session manager")
            
            // First try with session manager without checking isLoggedIn() which might be inaccurate
            val sessionUserData = app.sessionManager.getUserData()
            if (sessionUserData != null) {
                Log.d(TAG, "Found user data in session: ${sessionUserData.username}")
                userData = sessionUserData
                
                // Fix potential logged in state discrepancy
                if (!app.sessionManager.isLoggedIn()) {
                    Log.d(TAG, "Session reports not logged in but we have user data - fixing")
                    app.sessionManager.saveLoginState(sessionUserData.id.toLong())
                }
                
                // Update the app's user reference
                app.loggedInuser = userData
                app.isLoggedIn = true
            } 
            // If no data but session says logged in
            else if (app.sessionManager.isLoggedIn()) {
                Log.d(TAG, "Session manager reports logged in status but no user data")
                
                // Try fallback methods for user data
                val userId = app.sessionManager.getUserId()
                val username = app.sessionManager.getUsername() ?: "User"
                val email = app.sessionManager.getUserEmail() ?: "user@example.com"
                
                if (userId > 0) {
                    Log.d(TAG, "Creating minimal user from session data, userId: $userId")
                    userData = User(
                        id = userId,
                        username = username,
                        email = email
                    )
                    app.loggedInuser = userData
                    app.isLoggedIn = true
                }
            }
        }
        
        // Final check - if we still have no user data, we have to log out
        if (userData == null) {
            Log.e(TAG, "Could not retrieve user data after multiple attempts, redirecting to login")
            // If no user is logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // We have user data, proceed with it
        currentUser = userData
        Log.d(TAG, "Using user: ${currentUser.id}, ${currentUser.username}")

        // Save the current user back to session to ensure persistence
        app.sessionManager.saveUserData(currentUser)

        // Initialize UI components
        initializeViews()

        // Load wallet data
        Log.d(TAG, "Loading wallet for user: ${currentUser.userid}")
        walletViewModel.getWalletByUserId(currentUser.userid.toLong())

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
                Log.d(TAG, "Wallet updated: ${it.id}, balance: ${it.balance}")
                currentWallet = it
                updateUI()
            }
        }

        // Set up UI with user data
        updateUI()

        // Set up click listeners
        setupClickListeners()
        
        Log.d(TAG, "ProfileActivity onCreate completed")
    }

    private fun initializeViews() {
        // Find views by ID
        usernameTextView = findViewById(R.id.text_username)
        balanceTextView = findViewById(R.id.text_balance)
        emailTextView = findViewById(R.id.tv_email)

        // Menu items
        walletMenu = findViewById(R.id.menu_wallet)
        mailsMenu = findViewById(R.id.menu_mails)
        transactionsMenu = findViewById(R.id.menu_transactions)
        wishlistMenu = findViewById(R.id.menu_wishlist)
        searchMenu = findViewById(R.id.menu_search)
        settingsMenu = findViewById(R.id.menu_settings)
        logoutMenu = findViewById(R.id.menu_logout)
    }

    private fun updateUI() {
        // Set username and email
        usernameTextView.text = currentUser.username
        emailTextView.text = currentUser.email

        // Format wallet balance with commas and two decimal points
        val balance = currentWallet?.balance ?: 0.0
        val formattedBalance = String.format("%,.2f", balance)
        balanceTextView.text = "₱$formattedBalance"
    }

    private fun setupClickListeners() {
        // Wallet menu click
        walletMenu.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }

        // Menu item clicks
        mailsMenu.setOnClickListener {
            startActivity(Intent(this, MailsActivity::class.java))
        }

        transactionsMenu.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        wishlistMenu.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        searchMenu.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        settingsMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        logoutMenu.setOnClickListener {
            // Show logout confirmation dialog
            DialogUtils.showCustomConfirmationDialog(
                this,
                "Logout",
                "Are you sure you want to logout?",
                {
                    Log.d(TAG, "User confirmed logout")
                    // Log out the user completely
                    val app = application as CredigoApp
                    
                    // Clear all session data
                    app.sessionManager.clearLoginState()
                    app.isLoggedIn = false
                    app.loggedInuser = null
                    
                    // Navigate to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }
}
