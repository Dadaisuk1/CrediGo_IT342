package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.adapters.TransactionAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.fragments.NavbarFragment
import com.sia.credigo.viewmodel.TransactionViewModel

class TransactionActivity : AppCompatActivity() {
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var emptyStateView: TextView
    private lateinit var progressBar: ProgressBar
    
    private var currentUserId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        
        // Get user ID from CredigoApp
        val app = application as CredigoApp
        currentUserId = (app.loggedInuser?.userid ?: -1).toLong()
        
        // Initialize ViewModel
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        
        // Initialize views
        recyclerView = findViewById(R.id.rv_transactions)
        backButton = findViewById(R.id.iv_back)
        
        // Find or initialize empty state view and progress bar
        emptyStateView = findViewById<TextView>(R.id.empty_state)?.apply {
            text = "No transactions found"
            visibility = View.GONE
        } ?: TextView(this)
        
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)?.apply {
            visibility = View.VISIBLE
        } ?: ProgressBar(this)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Fetch transactions using the new history endpoint
        transactionViewModel.fetchTransactionHistory()
        
        // Observe loading state
        transactionViewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe error messages
        transactionViewModel.errorMessage.observe(this, Observer { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })
        
        // Observe transactions
        transactionViewModel.allTransactions.observe(this, Observer { transactions ->
            if (transactions.isEmpty()) {
                emptyStateView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                
                // Set up adapter with transactions and click handler
                recyclerView.adapter = TransactionAdapter(transactions) { transaction ->
                    // Handle transaction click - navigate to TransactionDetails
                    val intent = Intent(this, TransactionDetails::class.java).apply {
                        putExtra("TRANSACTION_ID", transaction.transaction_id)
                    }
                    startActivity(intent)
                }
            }
        })
        
        // Set header title and back button
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Transactions"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onBackPressed() }

        // Set up navbar
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh transaction data when returning to this screen
        transactionViewModel.fetchTransactionHistory()
    }
}