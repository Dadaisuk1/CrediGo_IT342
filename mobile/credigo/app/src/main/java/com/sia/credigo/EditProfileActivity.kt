package com.sia.credigo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import android.text.InputType
import android.app.ProgressDialog
import java.util.regex.Pattern
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class EditProfileActivity : AppCompatActivity() {
    companion object {
        // Define the constant here
        const val REQUEST_PROFILE_UPDATE = 1001
    }

    private lateinit var btnUpdate: Button
    private lateinit var usersViewModel: UserViewModel
    private lateinit var txtUsernameError: TextView
    private lateinit var txtPassword: EditText
    private lateinit var txtPhoneNumber: EditText
    private var usernameIsGood = false
    private var passwordIsGood = false
    private var phoneNumberIsGood = false

    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )

    private val PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,13}$"
    )

    private var progressDialog: ProgressDialog? = null

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

    private fun validatePassword(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val currentUser = (application as CredigoApp).loggedInuser

        if (currentUser == null) {
            finish()
            return
        }

        usersViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val txtEmail = findViewById<TextView>(R.id.txt_email)
        val txtUsername = findViewById<EditText>(R.id.edittext_username)
        txtPassword = findViewById(R.id.edittext_password)
        txtPhoneNumber = findViewById(R.id.edittext_phone_number)
        btnUpdate = findViewById(R.id.btn_update)
        txtUsernameError = findViewById(R.id.txt_username_error)
        val txtPhoneNumberError = findViewById<TextView>(R.id.txt_phone_number_error)
        val txtCancel = findViewById<TextView>(R.id.txt_cancel)
        val btnBack = findViewById<ImageView>(R.id.iv_back)
        val passwordToggle = findViewById<ImageView>(R.id.password_visibility_toggle)

        txtEmail.text = currentUser.email
        txtUsername.setText(currentUser.username)
        txtPassword.setText("")
        txtPhoneNumber.setText(currentUser.phonenumber)

        txtUsernameError.text = ""
        txtUsernameError.setTextColor(getColor(R.color.error_text))
        txtPhoneNumberError.text = ""
        txtPhoneNumberError.setTextColor(getColor(R.color.error_text))

        usernameIsGood = true
        updateButtonState()

        txtUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputUsername = s.toString()
                lifecycleScope.launch {
                    if (inputUsername.isEmpty()) {
                        txtUsernameError.text = "Username is required"
                        usernameIsGood = false
                    } else if (inputUsername != currentUser.username) {
                        val existingUser = usersViewModel.getUserByUsername(inputUsername)
                        if (existingUser != null) {
                            txtUsernameError.text = "Username already taken"
                            usernameIsGood = false
                        } else {
                            txtUsernameError.text = ""
                            usernameIsGood = true
                        }
                    } else {
                        txtUsernameError.text = ""
                        usernameIsGood = true
                    }
                    updateButtonState()
                }
            }
        })

        txtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newPassword = s.toString()
                if (newPassword.isEmpty()) {
                    passwordIsGood = true
                } else {
                    passwordIsGood = validatePassword(newPassword)
                }
                updateButtonState()
            }
        })

        txtPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phoneNumber = s.toString()
                if (phoneNumber.isEmpty()) {
                    txtPhoneNumberError.text = "Phone number is required"
                    phoneNumberIsGood = false
                } else if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                    txtPhoneNumberError.text = "Invalid phone number format"
                    phoneNumberIsGood = false
                } else {
                    txtPhoneNumberError.text = ""
                    phoneNumberIsGood = true
                }
                updateButtonState()
            }
        })

        passwordToggle.setOnClickListener {
            if (txtPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                txtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            } else {
                txtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_unhide)
            }
            txtPassword.setSelection(txtPassword.text.length)
        }

        btnUpdate.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Changes")
                .setMessage("Are you sure you want to update your profile?")
                .setPositiveButton("Update") { _, _ ->
                    showLoading()
                    val newUsername = txtUsername.text.toString()
                    val newPassword = txtPassword.text.toString().takeIf { it.isNotEmpty() }
                    val newPhoneNumber = txtPhoneNumber.text.toString()

                    if (newPassword != null && !validatePassword(newPassword)) {
                        Toast.makeText(this, "Password must contain 8+ chars with uppercase, lowercase, number and special char", Toast.LENGTH_LONG).show()
                        dismissLoading()
                        return@setPositiveButton
                    }

                    if (!PHONE_PATTERN.matcher(newPhoneNumber).matches()) {
                        Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_LONG).show()
                        dismissLoading()
                        return@setPositiveButton
                    }

                    lifecycleScope.launch {
                        try {
                            val success = usersViewModel.updateUser(
                                userId = currentUser.id.toLong(),
                                username = newUsername,
                                password = newPassword,
                                phonenumber = newPhoneNumber
                            )

                            if (success) {
                                Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            dismissLoading()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        txtCancel.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Inside the button click handler or wherever you're starting the confirmation activity:
        val intent = Intent(this, EditProfileConfirmationActivity::class.java).apply {
            putExtra("userId", currentUser.id.toLong())
            putExtra("username", txtUsername.text.toString())
            putExtra("newPassword", txtPassword.text.toString().takeIf { it.isNotEmpty() }) // Use "newPassword" key
            putExtra("phoneNumber", txtPhoneNumber.text.toString())
        }
        startActivityForResult(intent, REQUEST_PROFILE_UPDATE)
    }

    private fun updateButtonState() {
        val isValid = usernameIsGood && (passwordIsGood || txtPassword.text.isEmpty()) && phoneNumberIsGood
        btnUpdate.isEnabled = isValid
        btnUpdate.alpha = if (isValid) 1.0f else 0.5f
    }

    // Handle activity result 
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_PROFILE_UPDATE && resultCode == RESULT_OK) {
            val isSuccess = data?.getBooleanExtra("RESULT_UPDATE_SUCCESS", false) ?: false
            if (isSuccess) {
                // Profile updated successfully, close this activity
                finish()
            }
        }
    }
}
