
package com.sia.credigo
import android.content.Intent
import android.os.Bundle

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sia.credigo.adapters.MailAdapter
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.fragments.NavbarFragment
import com.sia.credigo.utils.DialogUtils
import com.sia.credigo.viewmodel.MailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MailsActivity : AppCompatActivity() {
    private var mailObserver: androidx.lifecycle.Observer<Int>? = null
    companion object {
        private const val MAIL_DETAIL_REQUEST_CODE = 100
    }
    private lateinit var mailViewModel: MailViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView

    private var currentUserId: Int = -1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAIL_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh mail list
            mailViewModel.getUserMails(currentUserId)
            // Set result to update parent activity
            setResult(RESULT_OK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mails)

        // Set up navbar
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()

        // Initialize ViewModels
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)

        // Get user ID from CredigoApp
        val app = application as CredigoApp
        currentUserId = app.loggedInuser?.userid ?: -1

        // Initialize ViewModel
        mailViewModel = ViewModelProvider(this).get(MailViewModel::class.java)

        // Initialize views
        recyclerView = findViewById(R.id.rv_mails)
        backButton = findViewById(R.id.iv_back)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get mails for the current user
        mailViewModel.getUserMails(currentUserId)

        // Observe mails
        mailViewModel.userMails.observe(this, Observer { mails ->
            recyclerView.adapter = MailAdapter(
                mails = mails,
                onMailClicked = { mail ->
                    // Handle mail click - navigate to MailDetailsActivity
                    val intent = Intent(this, MailDetailsActivity::class.java).apply {
                        putExtra("MAIL_ID", mail.mailid)
                    }
                    startActivityForResult(intent, MAIL_DETAIL_REQUEST_CODE)
                },
                onMailLongClicked = { mail ->
                    // Handle long press - show delete confirmation dialog
                    DialogUtils.showCustomConfirmationDialog(
                        context = this,
                        title = "Delete Mail",
                        message = "Are you sure you want to delete this mail?",
                        onConfirm = {
                            // Delete mail
                            CoroutineScope(Dispatchers.IO).launch {
                                mailViewModel.deleteMail(mail)

                                // Refresh mail list
                                runOnUiThread {
                                    mailViewModel.getUserMails(currentUserId)
                                    Toast.makeText(this@MailsActivity, "Mail deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    true
                }
            )
        })

        // Set header title and back button
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Mails"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onBackPressed() }

        // Set up mail observer
        mailObserver = androidx.lifecycle.Observer<Int> { count ->
            setResult(RESULT_OK) // Ensure parent activity updates its mail icon
        }

        // Initial mail check
        mailViewModel.unreadMailCount.observe(this, mailObserver!!)
    }
}
