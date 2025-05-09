package com.sia.credigo

import android.os.Bundle
import android.widget.ImageView
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

class TransactionDetails : AppCompatActivity() {
    companion object {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Set header title
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Transaction Details"

        // Set up navbar
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()

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

        // Get transaction ID from intent
        val transactionId = intent.getLongExtra("TRANSACTION_ID", -1)
        if (transactionId != -1L) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Get transaction details on IO thread
                    val transaction = withContext(Dispatchers.IO) {
                        transactionViewModel.getTransactionById(transactionId)
                    }

                    if (transaction != null) {
                        // Set initial transaction details on Main thread
                        productNameView.text = transaction.type
                        priceView.text = "₱${String.format("%.2f", transaction.amount)}"
                        transactionIdView.text = transaction.transaction_id.toString()

                        // Format date
                        val dateFormat = SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())
                        dateView.text = dateFormat.format(Date(transaction.timestamp))

                        // Find product by name and get its category
                        try {
                            val product = withContext(Dispatchers.IO) {
                                productViewModel.getProductByName(transaction.type)
                            }

                            if (product != null) {
                                // Get category name
                                val category = withContext(Dispatchers.IO) {
                                    categoryViewModel.getPlatformById(product.platformId)
                                }
                                categoryView.text = category?.name ?: "Unknown Category"
                            } else {
                                // Try to parse category from transaction type
                                val parts = transaction.type.split(" - ")
                                if (parts.size > 1) {
                                    productNameView.text = parts[0]
                                    categoryView.text = parts[1]
                                } else {
                                    categoryView.text = "Unknown Category"
                                }
                            }
                        } catch (e: Exception) {
                            categoryView.text = "Unknown Category"
                        }
                    }
                } catch (e: Exception) {
                    // Handle error loading transaction
                    Toast.makeText(
                        this@TransactionDetails,
                        "Error loading transaction details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        // Set header title and back button
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
}
