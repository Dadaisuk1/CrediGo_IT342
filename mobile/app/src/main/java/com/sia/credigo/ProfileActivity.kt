package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.manager.WalletManager
import com.sia.credigo.manager.WalletState
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.model.User
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.*

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var userViewModel: UserViewModel
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

        // Get current user from application
        val app = application as CredigoApp
        Log.d(TAG, "App instance retrieved, loggedInUser: ${app.loggedInuser != null}, isLoggedIn: ${app.sessionManager.isLoggedIn()}")
        
        // Check if user is logged in
        if (!app.isLoggedIn || app.loggedInuser == null) {
            Log.e(TAG, "User not logged in, redirecting to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Get current user data
        currentUser = app.loggedInuser!!
        Log.d(TAG, "Using user: ${currentUser.id}, ${currentUser.username}")

        // Initialize UI components
        initializeViews()

        // Observe wallet state from WalletManager
        WalletManager.walletState.observe(this) { state ->
            when (state) {
                is WalletState.Loading -> {
                    // Could show a loading indicator
                    Log.d(TAG, "Wallet loading...")
                }
                is WalletState.Loaded -> {
                    currentWallet = state.wallet
                    Log.d(TAG, "Wallet loaded: ${state.wallet.balance}")
                    updateUI()
                }
                is WalletState.Error -> {
                    Toast.makeText(this, "Error loading wallet: ${state.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Wallet error: ${state.message}")
                }
                else -> { /* Handle other states if needed */ }
            }
        }
        
        // Refresh wallet data
        WalletManager.refreshWallet()

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
        val balance = currentWallet?.balance?.toDouble() ?: 0.0
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
                    app.logout()
                    
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
        Log.d(TAG, "onResume called - refreshing wallet data")
        // Refresh wallet data when returning to this screen
        WalletManager.refreshWallet()
    }
}
