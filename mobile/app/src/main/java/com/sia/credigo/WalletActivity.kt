package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.fragments.NavbarFragment
import com.sia.credigo.model.Transaction
import com.sia.credigo.model.TransactionType
import com.sia.credigo.model.User
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.TransactionViewModel
import com.sia.credigo.viewmodel.UserViewModel
import com.sia.credigo.viewmodel.WalletViewModel
import kotlinx.coroutines.*

class WalletActivity : AppCompatActivity() {
    // ViewModels
    private lateinit var userViewModel: UserViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    
    // User and wallet data
    private lateinit var currentUser: User
    private var currentWallet: Wallet? = null

    // UI components
    private lateinit var backButton: ImageView
    private lateinit var balanceTextView: TextView
    private lateinit var amountEditText: EditText
    private lateinit var cashInButton: Button
    private lateinit var limitWarningText: TextView
    private lateinit var selectionErrorText: TextView

    // Payment options
    private lateinit var gcashOption: LinearLayout
    private lateinit var mayaOption: LinearLayout
    private lateinit var visaOption: LinearLayout
    private lateinit var mastercardOption: LinearLayout

    // Menu items
    private lateinit var transactionsMenu: LinearLayout
    private lateinit var depositHistoryMenu: LinearLayout

    // Selected payment option
    private var selectedPaymentOption: String? = null
    private var selectedPaymentView: LinearLayout? = null

    // Coroutine scope
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        // Initialize ViewModels
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        walletViewModel = ViewModelProvider(this).get(WalletViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        // Get current user from application
        val app = application as CredigoApp
        app.loggedInuser?.let {
            currentUser = it
        } ?: run {
            // If no user is logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize UI components
        initializeViews()

        // Load wallet data
        walletViewModel.getWalletByUserId(currentUser.userid.toLong())

        // Load the latest user data
        lifecycleScope.launch {
            userViewModel.loadUser(currentUser.userid)
        }

        // Observe wallet data for live updates
        walletViewModel.userWallet.observe(this) { wallet ->
            wallet?.let {
                currentWallet = it
                updateUI()
            }
        }

        // Observe user data for live updates
        userViewModel.currentUser.observe(this) { user ->
            user?.let {
                currentUser = it
                // Update the app's logged in user
                (application as CredigoApp).loggedInuser = it
                // Reload wallet data if user changes
                walletViewModel.getWalletByUserId(currentUser.userid.toLong())
            }
        }

        // Set up click listeners
        setupClickListeners()

        // Set up text watchers
        setupTextWatchers()

        // Set up navbar
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()
    }

    private fun initializeViews() {
        // Toolbar and navigation
        backButton = findViewById(R.id.iv_back)

        // Balance and cash in components
        balanceTextView = findViewById(R.id.text_balance)
        amountEditText = findViewById(R.id.edit_amount)
        cashInButton = findViewById(R.id.btn_cash_in)
        limitWarningText = findViewById(R.id.text_limit_warning)
        selectionErrorText = findViewById(R.id.text_selection_error)

        // Payment options
        gcashOption = findViewById(R.id.option_gcash)
        mayaOption = findViewById(R.id.option_maya)
        visaOption = findViewById(R.id.option_visa)
        mastercardOption = findViewById(R.id.option_mastercard)

        // Menu items
        transactionsMenu = findViewById(R.id.menu_transactions)
        depositHistoryMenu = findViewById(R.id.menu_deposit_history)
    }

    private fun updateUI() {
        // Format wallet balance with commas and two decimal points
        val balance = currentWallet?.balance ?: 0.0
        val formattedBalance = String.format("%,.2f", balance)
        balanceTextView.text = "₱$formattedBalance"

        // Initially disable the cash in button
        cashInButton.isEnabled = false
    }

    private fun setupClickListeners() {
        // Back button click
        backButton.setOnClickListener {
            finish()
        }

        // Payment option clicks
        gcashOption.setOnClickListener {
            selectPaymentOption("GCash", gcashOption)
        }

        mayaOption.setOnClickListener {
            selectPaymentOption("Maya", mayaOption)
        }

        visaOption.setOnClickListener {
            selectPaymentOption("Visa", visaOption)
        }

        mastercardOption.setOnClickListener {
            selectPaymentOption("Mastercard", mastercardOption)
        }

        // Cash in button click
        cashInButton.setOnClickListener {
            val amountText = amountEditText.text.toString()
            if (amountText.isNotEmpty() && selectedPaymentOption != null) {
                try {
                    val amount = amountText.toDouble()
                    if (amount > 0) {
                        // Get current wallet
                        val wallet = currentWallet
                        if (wallet == null) {
                            Toast.makeText(this, "Wallet not found", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Check if new balance would exceed 1,000,000
                        val newBalance = wallet.balance + amount
                        if (newBalance > 1000000) {
                            // Already showing warning via text watcher
                        } else {
                            // Disable button to prevent multiple clicks
                            cashInButton.isEnabled = false

                            // Create deposit instance
                            coroutineScope.launch {
                                val success = createDeposit(amount)

                                if (success) {
                                    // Update wallet balance
                                    walletViewModel.updateWalletBalance(wallet.id.toLong(), newBalance)

                                    // Update UI
                                    // updateUI() will be called by the observer

                                    // Clear input field and selection
                                    amountEditText.text.clear()
                                    deselectAllPaymentOptions()

                                    // Reset error message states
                                    limitWarningText.visibility = View.INVISIBLE
                                    selectionErrorText.visibility = View.INVISIBLE

                                    Toast.makeText(this@WalletActivity, "Successfully added ₱$amount to your wallet", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Re-enable button if deposit failed
                                    checkCashInButtonStatus()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (selectedPaymentOption == null) {
                    selectionErrorText.visibility = View.VISIBLE
                }
                if (amountText.isEmpty()) {
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Menu item clicks
        transactionsMenu.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        depositHistoryMenu.setOnClickListener {
            startActivity(Intent(this, DepositHistoryActivity::class.java))
        }
    }

    private fun setupTextWatchers() {
        // Ensure error message spaces are always maintained
        limitWarningText.visibility = View.INVISIBLE
        selectionErrorText.visibility = View.INVISIBLE

        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val amountText = s.toString()
                if (amountText.isNotEmpty()) {
                    try {
                        val amount = amountText.toDouble()
                        val currentBalance = currentWallet?.balance ?: 0.0
                        val newBalance = currentBalance + amount

                        // Check if new balance exceeds limit
                        if (newBalance > 1000000) {
                            limitWarningText.visibility = View.VISIBLE
                            selectionErrorText.visibility = View.INVISIBLE
                            cashInButton.isEnabled = false
                        } else {
                            limitWarningText.visibility = View.INVISIBLE
                            // Show selection error if amount entered but no option selected
                            selectionErrorText.visibility = if (selectedPaymentOption == null) View.VISIBLE else View.INVISIBLE
                            checkCashInButtonStatus()
                        }
                    } catch (e: NumberFormatException) {
                        limitWarningText.visibility = View.INVISIBLE
                        selectionErrorText.visibility = View.INVISIBLE
                        checkCashInButtonStatus()
                    }
                } else {
                    limitWarningText.visibility = View.INVISIBLE
                    selectionErrorText.visibility = View.INVISIBLE
                    checkCashInButtonStatus()
                }
            }
        })
    }

    private fun selectPaymentOption(option: String, view: LinearLayout) {
        // Deselect previous option
        selectedPaymentView?.setBackgroundResource(R.drawable.item_background)

        // Select new option
        selectedPaymentOption = option
        selectedPaymentView = view
        view.setBackgroundResource(R.drawable.item_wallet_option_selected)

        // Hide selection error
        selectionErrorText.visibility = View.INVISIBLE

        // Enable cash in button if amount is entered
        checkCashInButtonStatus()
    }

    private fun deselectAllPaymentOptions() {
        // Reset all payment options
        val options = listOf(gcashOption, mayaOption, visaOption, mastercardOption)
        for (option in options) {
            option.setBackgroundResource(R.drawable.item_background)
        }

        selectedPaymentOption = null
        selectedPaymentView = null

        // Show selection error if amount is entered
        if (amountEditText.text.toString().isNotEmpty()) {
            selectionErrorText.visibility = View.VISIBLE
        }

        // Disable cash in button
        cashInButton.isEnabled = false
    }

    private fun checkCashInButtonStatus() {
        val amountText = amountEditText.text.toString()

        val isEnabled = amountText.isNotEmpty() &&
                selectedPaymentOption != null &&
                limitWarningText.visibility != View.VISIBLE

        cashInButton.isEnabled = isEnabled

        // Change button appearance based on enabled state
        if (isEnabled) {
            cashInButton.alpha = 1.0f
        } else {
            cashInButton.alpha = 0.5f
        }
    }

    private suspend fun createDeposit(amount: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure we have a valid payment option
                if (selectedPaymentOption == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletActivity, "Please select a payment method", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext false
                }

                val transaction = Transaction(
                    userid = currentUser.userid.toLong(),
                    type = selectedPaymentOption!!,  // We know it's not null here
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    transactionType = TransactionType.DEPOSIT
                )

                // Create the transaction
                transactionViewModel.createTransaction(transaction)

                // For now, consider it successful if no exception is thrown
                // In a real app, you'd check the response from createTransaction
                return@withContext true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletActivity, "Error creating deposit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
