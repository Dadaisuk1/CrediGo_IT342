package com.sia.credigo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.adapters.DepositAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.TransactionType
import com.sia.credigo.viewmodel.*
import com.sia.credigo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DepositHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var transactionViewModel: TransactionViewModel
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit_history)

        // Initialize views
        recyclerView = findViewById(R.id.rv_deposits)
        emptyStateView = findViewById(R.id.empty_state)

        // Set header title
        findViewById<TextView>(R.id.tv_header_title).text = "Deposit History"

        // Set up back button
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize ViewModel
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        // Load deposit transactions
        loadDepositTransactions()
    }

    private fun loadDepositTransactions() {
        // Get current user ID
        val app = application as CredigoApp
        val userId = app.loggedInuser?.userid?.toLong() ?: -1L

        if (userId > 0) {
            // Fetch all transactions for the user
            transactionViewModel.getUserTransactions(userId)

            // Observe transactions and filter for deposits
            transactionViewModel.allTransactions.observe(this) { transactions ->
                // Filter transactions with DEPOSIT type
                val depositTransactions = transactions.filter { 
                    it.transactionType == TransactionType.DEPOSIT 
                }

                if (depositTransactions.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateView.visibility = View.GONE
                    recyclerView.adapter = DepositAdapter(depositTransactions)
                }
            }
        } else {
            // No user ID available
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
} 
