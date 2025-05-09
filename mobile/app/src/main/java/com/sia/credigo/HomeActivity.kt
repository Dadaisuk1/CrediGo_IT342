package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sia.credigo.adapters.GameCategoryAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.MailsActivity
import com.sia.credigo.viewmodel.*
import com.sia.credigo.model.*
import com.sia.credigo.model.Wallet

import android.view.View
import android.widget.Toast

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val MAIL_LIST_REQUEST_CODE = 100
        private const val TAG = "HomeActivity"
    }
    private var mailObserver: androidx.lifecycle.Observer<Int>? = null
    private lateinit var viewModel: PlatformViewModel
    private lateinit var mailViewModel: MailViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var walletBalanceView: TextView
    private lateinit var walletIcon: ImageView
    private lateinit var mailIcon: ImageView

    private var currentUserId: Int = -1
    private var currentWallet: Wallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Log.d(TAG, "onCreate: Starting HomeActivity initialization")

        // Get user ID from CredigoApp
        val app = application as CredigoApp
        currentUserId = app.loggedInuser?.userid ?: -1
        Log.d(TAG, "Current user ID: $currentUserId")

        // Initialize ViewModels
        viewModel = ViewModelProvider(this).get(PlatformViewModel::class.java)
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Initialize views
        recyclerView = findViewById(R.id.rv_game_categories)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        walletBalanceView = findViewById(R.id.tv_balance)
        walletIcon = findViewById(R.id.iv_wallet)
        mailIcon = findViewById(R.id.mail_icon)

        // Setup RecyclerView
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            setHasFixedSize(true)
        }

        // Setup refresh capability
        findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)?.setOnRefreshListener {
            Log.d(TAG, "Refresh triggered, fetching platforms")
            viewModel.fetchPlatforms()
        }

        // Load wallet data for current user
        if (currentUserId > 0) {
            Log.d(TAG, "Loading wallet for user ID: $currentUserId")
            walletViewModel.getWalletByUserId(currentUserId.toLong())
        }

        // Fetch platforms from backend
        Log.d(TAG, "Fetching platforms from backend")
        viewModel.fetchPlatforms()

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
                Log.d(TAG, "Wallet updated: balance = ${it.balance}")
                currentWallet = it
                // Update balance display with commas and two decimal points
                val formattedBalance = String.format("%,.2f", it.balance)
                walletBalanceView.text = "₱$formattedBalance"
            }
        }

        // Set up wallet icon click listener
        walletIcon.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }

        // Set up mail icon click listener
        mailIcon.setOnClickListener {
            startActivityForResult(Intent(this, MailsActivity::class.java), MAIL_LIST_REQUEST_CODE)
        }

        // Set up mail observer
        mailObserver = androidx.lifecycle.Observer<Int> { count ->
            updateMailIcon(count, mailViewModel.hasPurchaseMail.value ?: false)
        }

        // Initial mail check
        mailViewModel.unreadMailCount.observe(this, mailObserver!!)

        // Observe purchase mail status
        mailViewModel.hasPurchaseMail.observe(this) { hasPurchaseMail ->
            updateMailIcon(mailViewModel.unreadMailCount.value ?: 0, hasPurchaseMail)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            Log.d(TAG, "Loading state changed: $isLoading")
            findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)?.isRefreshing = isLoading
            findViewById<View>(R.id.progress_bar)?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Log.e(TAG, "Error from ViewModel: $it")
                // Show error message to user
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observe platforms and set up adapter
        viewModel.allPlatforms.observe(this, Observer { platforms ->
            if (!platforms.isNullOrEmpty()) {
                Log.d(TAG, "Platforms loaded: ${platforms.size}")
                platforms.forEach { platform ->
                    Log.d(TAG, "Platform: id=${platform.id}, name=${platform.name}, Logo: ${platform.logoUrl}")
                }
                
                try {
                    // Create and set adapter
                    val adapter = GameCategoryAdapter(platforms)
                    recyclerView.adapter = adapter
                    recyclerView.visibility = View.VISIBLE
                    findViewById<View>(R.id.empty_state_view)?.visibility = View.GONE
                    
                    Log.d(TAG, "Adapter set with ${platforms.size} platforms")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting up adapter: ${e.message}", e)
                    Toast.makeText(this, "Error displaying platforms", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "No platforms found or empty platform list")
                recyclerView.visibility = View.GONE
                findViewById<View>(R.id.empty_state_view)?.visibility = View.VISIBLE
            }
        })
        
        Log.d(TAG, "HomeActivity onCreate completed")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAIL_LIST_REQUEST_CODE && resultCode == RESULT_OK) {
            // Force refresh mail count
            mailViewModel.updateUnreadMailCount()
        }
    }

    private fun updateMailIcon(unreadCount: Int, hasPurchaseMail: Boolean) {
        if (unreadCount > 0) {
            // If there are unread mails, use the unread icon
            mailIcon.setImageResource(R.drawable.ic_mail_unread)
        } else {
            // If there are no unread mails, use the regular icon
            mailIcon.setImageResource(R.drawable.ic_mail)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        
        // Remove and re-add observer to force refresh
        mailObserver?.let { observer ->
            mailViewModel.unreadMailCount.removeObserver(observer)
            mailViewModel.unreadMailCount.observe(this, observer)
        }

        // Force refresh purchase mail status
        mailViewModel.hasPurchaseMail.removeObservers(this)
        mailViewModel.hasPurchaseMail.observe(this) { hasPurchaseMail ->
            updateMailIcon(mailViewModel.unreadMailCount.value ?: 0, hasPurchaseMail)
        }
        
        // Refresh platforms in case data changed
        viewModel.fetchPlatforms()
    }
}
