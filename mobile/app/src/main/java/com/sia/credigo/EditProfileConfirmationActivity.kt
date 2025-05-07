package com.sia.credigo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.ProfileUpdateRequest
import com.sia.credigo.network.RetrofitClient
import kotlinx.coroutines.launch
import com.sia.credigo.network.models.UpdateUserRequest

class EditProfileConfirmationActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvPassword: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_confirmation)

        // Get data from intent
        val username = intent.getStringExtra("username")
        // Change from "password" to the correct key used in the intent
        val password = intent.getStringExtra("newPassword") // Updated key name
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val userId = intent.getLongExtra("userId", -1)

        if (userId == -1L) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        tvUsername = findViewById(R.id.edittext_username)
        tvPassword = findViewById(R.id.edittext_password)
        tvPhoneNumber = findViewById(R.id.edittext_phone_number)
        btnConfirm = findViewById(R.id.btn_confirm)
        btnCancel = findViewById(R.id.btn_cancel)

        // Set text values
        tvUsername.text = username ?: "No change"
        tvPassword.text = if (!password.isNullOrEmpty()) "••••••••" else "No change"
        tvPhoneNumber.text = phoneNumber ?: "No change"

        // Set up button clicks
        btnConfirm.setOnClickListener {
            updateProfile(userId, username, password, phoneNumber)
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun showLoading() {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Updating profile...")
            setCancelable(false)
            show()
        }
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
    }

    private fun updateProfile(userId: Long, username: String?, password: String?, phoneNumber: String?) {
        showLoading()

        val profileUpdateRequest = ProfileUpdateRequest(
            username = username,
            password = password,
            phoneNumber = phoneNumber
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.updateProfile(userId, ProfileUpdateRequest())

                if (response.isSuccessful && response.body() != null) {
                    // Update local user data
                    val app = application as CredigoApp
                    val currentUser = app.loggedInuser
                    if (currentUser != null) {
                        app.loggedInuser = currentUser.copy(
                            username = username ?: currentUser.username,
                            phonenumber = phoneNumber ?: currentUser.phonenumber
                        )
                    }

                    Toast.makeText(this@EditProfileConfirmationActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    
                    // Send result back to profile activity
                    val resultIntent = Intent()
                    resultIntent.putExtra("RESULT_UPDATE_SUCCESS", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@EditProfileConfirmationActivity, "Update failed: $errorMsg", Toast.LENGTH_LONG).show()
                    setResult(RESULT_CANCELED)
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileConfirmationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            } finally {
                dismissLoading()
            }
        }
    }
}