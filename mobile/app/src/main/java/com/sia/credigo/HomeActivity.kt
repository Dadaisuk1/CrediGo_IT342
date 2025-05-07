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
import com.sia.credigo.adapters.GameCategoryAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.MailsActivity
import com.sia.credigo.viewmodel.*
import com.sia.credigo.model.*
import com.sia.credigo.model.Wallet

import android.view.View

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val MAIL_LIST_REQUEST_CODE = 100
    }
    private var mailObserver: androidx.lifecycle.Observer<Int>? = null
    private lateinit var viewModel: PlatformViewModel
    private lateinit var mailViewModel: MailViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var walletBalanceView: TextView
    private lateinit var walletIcon: ImageView
    private lateinit var mailIcon: ImageView

    private var currentUserId: Int = -1
    private var currentWallet: Wallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Get user ID from CredigoApp
        val app = application as CredigoApp

        currentUserId = app.loggedInuser?.userid ?: -1

        // Initialize ViewModels
        viewModel = ViewModelProvider(this).get(PlatformViewModel::class.java)
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)

        // Initialize views
        recyclerView = findViewById(R.id.rv_game_categories)
        walletBalanceView = findViewById(R.id.tv_balance)
        walletIcon = findViewById(R.id.iv_wallet)
        mailIcon = findViewById(R.id.mail_icon)

        // Load wallet data for current user
        if (currentUserId > 0) {
            walletViewModel.getWalletByUserId(currentUserId.toLong())
        }

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
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

        // Set up RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        // Observe platforms and set up adapter
        viewModel.allPlatforms.observe(this, Observer { platforms ->
            if (!platforms.isNullOrEmpty()) {
                Log.d("HomeActivity", "Platforms loaded: ${platforms.size}")
                platforms.forEach { platform ->
                    Log.d("HomeActivity", "Platform: ${platform.name}, Logo: ${platform.logoUrl}")
                    // Optionally check for logo resource if needed
                }
                recyclerView.adapter = GameCategoryAdapter(platforms)
            } else {
                Log.e("HomeActivity", "No platforms found")
            }
        })
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
    }
}
