package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.User
import com.sia.credigo.model.Wallet
import com.sia.credigo.viewmodel.UserViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUser: User

    // Menu items
    private lateinit var editProfileMenu: LinearLayout
    private lateinit var aboutUsMenu: LinearLayout
    private lateinit var contactUsMenu: LinearLayout
    private lateinit var developersMenu: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

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

        // Set up click listeners
        setupClickListeners()

        // Set header title
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Settings"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        // Menu items
        editProfileMenu = findViewById(R.id.menu_edit_profile)
        aboutUsMenu = findViewById(R.id.menu_about_us)
        contactUsMenu = findViewById(R.id.menu_contact_us)
    }

    private fun setupClickListeners() {
        // Menu item clicks
        editProfileMenu.setOnClickListener {
            startActivity(Intent(this, EditProfileConfirmationActivity::class.java))
        }

        aboutUsMenu.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        contactUsMenu.setOnClickListener {
            startActivity(Intent(this, ContactUsActivity::class.java))
        }


    }

    override fun onBackPressed() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }
}