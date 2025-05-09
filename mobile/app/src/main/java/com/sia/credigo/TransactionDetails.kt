package com.sia.credigo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.fragments.NavbarFragment
import com.sia.credigo.viewmodel.PlatformViewModel
import com.sia.credigo.viewmodel.TransactionViewModel
import com.sia.credigo.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class TransactionDetails : AppCompatActivity() {
    companion object {
        private const val TAG = "TransactionDetails"
        private const val MAIL_LIST_REQUEST_CODE = 100
    }
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var categoryViewModel: PlatformViewModel

    // UI components
    private lateinit var backButton: ImageView
    private lateinit var productNameView: TextView
    private lateinit var priceView: TextView
    private lateinit var transactionIdView: TextView
    private lateinit var categoryView: TextView
    private lateinit var dateView: TextView
    private var statusView: TextView? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Set header title
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Transaction Details"

        // Initialize ViewModels
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(PlatformViewModel::class.java)

        // Initialize views
        backButton = findViewById(R.id.iv_back)
        productNameView = findViewById(R.id.tv_product_name_header)
        priceView = findViewById(R.id.tv_price_header)
        transactionIdView = findViewById(R.id.tv_transaction_id_value)
        categoryView = findViewById(R.id.tv_category_value)
        dateView = findViewById(R.id.tv_date_value)
        
        // Optional views that might not exist in the layout
        progressBar = findViewById<ProgressBar>(R.id.progress_bar) ?: ProgressBar(this).apply { visibility = View.GONE }
        
        progressBar.visibility = View.VISIBLE

        // Get transaction ID from intent
        val transactionId = intent.getLongExtra("TRANSACTION_ID", -1)
        if (transactionId != -1L) {
            loadTransactionDetails(transactionId)
        } else {
            // No transaction ID provided
            Toast.makeText(this, "Transaction ID not found", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
        
        // Set back button
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onBackPressed() }

        // Set up navbar fragment with CredigoApp instance
        val app = application as CredigoApp
        val navbarFragment = NavbarFragment().apply {
            arguments = Bundle().apply {
                putLong("USER_ID", (app.loggedInuser?.userid ?: -1).toLong())
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, navbarFragment)
            .commit()
    }
    
    private fun loadTransactionDetails(transactionId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                // First check if we have this transaction in the history list
                val cachedTransaction = transactionViewModel.transactionHistory.value?.find { 
                    it.transactionId.toLong() == transactionId 
                }
                
                if (cachedTransaction != null) {
                    // Use the transaction from the history list
                    displayTransactionResponse(cachedTransaction)
                } else {
                    // Get transaction details on IO thread
                    val transaction = withContext(Dispatchers.IO) {
                        transactionViewModel.getTransactionById(transactionId)
                    }

                    if (transaction != null) {
                        // Display transaction details
                        displayTransaction(transaction)
                    } else {
                        Toast.makeText(
                            this@TransactionDetails,
                            "Transaction not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading transaction", e)
                Toast.makeText(
                    this@TransactionDetails,
                    "Error loading transaction details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun displayTransactionResponse(transactionResponse: com.sia.credigo.model.TransactionResponse) {
        // Set product name
        productNameView.text = transactionResponse.productName
        
        // Set price with formatting
        val formattedPrice = String.format("₱%.2f", transactionResponse.totalAmount)
        priceView.text = formattedPrice
        
        // Set transaction ID
        transactionIdView.text = transactionResponse.transactionId.toString()
        
        // Format date
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val displayFormat = SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())
            val date = isoFormat.parse(transactionResponse.transactionTimestamp)
            dateView.text = if (date != null) displayFormat.format(date) else transactionResponse.transactionTimestamp
        } catch (e: Exception) {
            dateView.text = transactionResponse.transactionTimestamp
        }
        
        // Set category (platform name if available)
        categoryView.text = "Game Purchase" // Default
        
        // Get more platform details if possible 
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val platform = withContext(Dispatchers.IO) {
                    categoryViewModel.getPlatformById(transactionResponse.productId)
                }
                if (platform != null) {
                    categoryView.text = platform.name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching platform details", e)
            }
        }
    }
    
    private fun displayTransaction(transaction: com.sia.credigo.model.Transaction) {
        // Set product name - parse from type if needed
        val parts = transaction.type.split(" - ")
        if (parts.size > 1) {
            productNameView.text = parts[0]
            categoryView.text = parts[1]
        } else {
            productNameView.text = transaction.type
            categoryView.text = "Game Purchase"
        }
        
        // Set price with formatting
        priceView.text = "₱${String.format("%.2f", transaction.amount)}"
        
        // Set transaction ID
        transactionIdView.text = transaction.transaction_id.toString()
        
        // Format date
        val dateFormat = SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())
        dateView.text = dateFormat.format(Date(transaction.timestamp))
    }
}
