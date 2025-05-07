package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.model.User
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.*

class ProfileActivity : AppCompatActivity() {
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

        // Initialize ViewModels
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Get current user from application
        val app = application as CredigoApp
        currentUser = app.loggedInuser ?: run {
            // If no user is logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize UI components
        initializeViews()

        // Load wallet data
        walletViewModel.getWalletByUserId(currentUser.userid.toLong())

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
                currentWallet = it
                updateUI()
            }
        }

        // Set up UI with user data
        updateUI()

        // Set up click listeners
        setupClickListeners()
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
                    // Log out the user
                    (application as CredigoApp).loggedInuser = null

                    // Navigate to login screen
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}
