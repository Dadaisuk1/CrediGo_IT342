package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sia.credigo.utils.DialogUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.Mail
import com.sia.credigo.viewmodel.MailViewModel
import com.sia.credigo.viewmodel.ProductViewModel
import com.sia.credigo.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

class MailDetailsActivity : AppCompatActivity() {
    private lateinit var mailViewModel: MailViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var backButton: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var subjectTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var currentMail: Mail

    private var mailId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_details)

        // Get mail ID from intent
        mailId = intent.getLongExtra("MAIL_ID", -1)
        if (mailId == -1L) {
            finish()
            return
        }

        // Set header title
        val headerTitle = findViewById<TextView>(R.id.tv_title)
        headerTitle.text = "Mail Details"

        // Initialize ViewModels
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        // Initialize views
        backButton = findViewById(R.id.iv_back)
        deleteButton = findViewById(R.id.iv_delete)
        subjectTextView = findViewById(R.id.tv_subject)
        messageTextView = findViewById(R.id.tv_message)

        // Load mail details
        loadMailDetails()

        // Set up back button
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set up delete button
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Set up delete button
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun loadMailDetails() {
        lifecycleScope.launch {
            try {
                val mail = mailViewModel.getMailById(mailId)
                if (mail != null) {
                    currentMail = mail

                    // Mark mail as read immediately when opened
                    if (!mail.isRead) {
                        mail.isRead = true
                        mailViewModel.updateMail(mail)
                    }
                    // Always set result to refresh unread count in previous screen
                    setResult(RESULT_OK)

                    // Update UI with mail details
                    subjectTextView.text = mail.subject

                    // If this mail is associated with a transaction, get product and category info
                    if (mail.transaction_id != null) {
                        val transactionId = mail.transaction_id
                        val transaction = transactionViewModel.getTransactionById(transactionId)

                        if (transaction != null) {
                            // Get product name from transaction type
                            val productName = transaction.type

                            // Try to find the product in the database
                            val product = productViewModel.getProductByName(productName)

                            // If product found, get its category
                            var categoryName = ""
                            if (product != null) {
                                val category = productViewModel.getPlatformById(product.platformid)
                                if (category != null) {
                                    categoryName = category.name
                                }
                            }

                            // Inject product name and category into message
                            val message = mail.message.replace("[product name]", productName)
                                .replace("[category]", categoryName)
                            messageTextView.text = message
                        } else {
                            messageTextView.text = mail.message
                        }

                        // Add button to view transaction details
                        val transactionButton = findViewById<TextView>(R.id.btn_view_transaction)
                        transactionButton?.apply {
                            text = "Purchase Receipt"
                            setTextColor(resources.getColor(R.color.blue, null))
                            setOnClickListener {
                                val intent = Intent(
                                    this@MailDetailsActivity,
                                    TransactionDetails::class.java
                                ).apply {
                                    putExtra("TRANSACTION_ID", transactionId as Long)
                                }
                                startActivity(intent)
                            }
                            visibility = android.view.View.VISIBLE
                        }
                    } else {
                        messageTextView.text = mail.message
                    }
                } else {
                    finish()
                }
            } catch (e: Exception) {
                // Handle any exceptions
                Toast.makeText(this@MailDetailsActivity, "Error loading mail: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        DialogUtils.showCustomConfirmationDialog(
            context = this,
            title = "Delete Mail",
            message = "Are you sure you want to delete this mail?",
            onConfirm = {
                deleteMail()
            }
        )
    }

    private fun deleteMail() {
        lifecycleScope.launch {
            mailViewModel.deleteMail(currentMail)
            runOnUiThread {
                Toast.makeText(this@MailDetailsActivity, "Mail deleted", Toast.LENGTH_SHORT).show()
            }
            setResult(RESULT_OK)
            finish()
        }
    }
}
