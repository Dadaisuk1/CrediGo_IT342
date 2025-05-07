package com.sia.credigo

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.network.RetrofitClient
import com.sia.credigo.network.AuthApi
import com.sia.credigo.model.LoginRequest
import com.sia.credigo.viewmodel.*
import kotlinx.coroutines.*
import com.sia.credigo.viewmodel.UserViewModel
import java.net.SocketTimeoutException
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var btnLogin: Button
    private lateinit var txtEmailError: TextView
    private lateinit var txtPasswordError: TextView
    private var emailIsGood = false
    private var passwordIsGood = false
    private val authApi = RetrofitClient.retrofit.create(AuthApi::class.java)
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val app = application as CredigoApp
        if (app.sessionManager.isLoggedIn()) {
            // User is already logged in, redirect to home
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // Initialize view model
        userViewModel = UserViewModel(application as CredigoApp)

        // Set up views
        val txtEmail = findViewById<EditText>(R.id.edittext_email)
        val txtPassword = findViewById<EditText>(R.id.edittext_password)
        val passwordToggle = findViewById<ImageView>(R.id.password_visibility_toggle)
        btnLogin = findViewById(R.id.btn_login)
        txtEmailError = findViewById(R.id.txt_email_error)
        txtPasswordError = findViewById(R.id.txt_password_error)
        val txtRegister = findViewById<TextView>(R.id.txt_register)

        // Initially hide error messages
        txtEmailError.text = ""
        txtPasswordError.text = ""
        txtEmailError.setTextColor(getColor(R.color.error_text))
        txtPasswordError.setTextColor(getColor(R.color.error_text))

        // Initially disable button
        updateButtonState()

        // Email validation
        txtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().lowercase()
                lifecycleScope.launch {
                    if (email.isEmpty()) {
                        txtEmailError.text = "Email is required"
                        emailIsGood = false
                    } else if (!isValidEmail(email)) {
                        txtEmailError.text = "Invalid Email"
                        emailIsGood = false
                    } else {
                        // For login, we don't care if the email is registered or not at this point
                        // We'll check that when the button is pressed
                            txtEmailError.text = ""
                            emailIsGood = true
                    }
                    updateButtonState()
                }
            }
        })

        // Password validation
        txtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isEmpty()) {
                    txtPasswordError.text = "Password is required"
                    passwordIsGood = false
                } else {
                    txtPasswordError.text = ""
                    passwordIsGood = true
                }
                updateButtonState()
            }
        })

        // Password visibility toggle
        passwordToggle.setOnClickListener {
            // Toggle password visibility
            if (txtPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                // Hide password
                txtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            } else {
                // Show password
                txtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_unhide)
            }
            // Move cursor to the end
            txtPassword.setSelection(txtPassword.text.length)
        }

        // Login button click
        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString().lowercase()
            val password = txtPassword.text.toString()

            // Disable button and show loading state
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."
            
            // Clear previous errors
            txtPasswordError.text = ""

            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Attempting to login with email: $email")
                    
                    // Try the production URL if connected to Wi-Fi or mobile data
                    val response = try {
                        withTimeout(15000) { // 15 seconds timeout
                            authApi.login(LoginRequest(email, password))
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.e(TAG, "Login timeout, trying alternate server", e)
                        // If timeout, try alternate server
                        try {
                            withTimeout(15000) {
                                RetrofitClient.getAlternateRetrofit().create(AuthApi::class.java)
                                    .login(LoginRequest(email, password))
                            }
                        } catch (e2: Exception) {
                            Log.e(TAG, "Alternate server also failed", e2)
                            throw e // Throw original exception if alternate also fails
                        }
                    }

                    if (!response.isSuccessful) {
                        val errorMessage = when (response.code()) {
                            401 -> "Invalid email or password"
                            403 -> "Account is locked. Please contact support."
                            404 -> "User not found"
                            500 -> "Server error. Please try again later."
                            else -> "Login failed: ${response.code()}"
                        }
                        
                        txtPasswordError.text = errorMessage
                        passwordIsGood = false
                        updateButtonState()
                        
                        // Reset button state
                        btnLogin.isEnabled = true
                        btnLogin.text = "Login"
                        return@launch
                    }

                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d(TAG, "Login successful")
                        
                        // Set current user in CredigoApp
                        (application as CredigoApp).apply {
                            // Save login data including token
                            sessionManager.saveLoginData(loginResponse)
                            isLoggedIn = true

                            // We don't have the full user object yet, just the ID
                            // Set loggedInuser to null for now, it will be fetched later
                            loggedInuser = null
                        }

                        // Navigate to home screen
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        txtPasswordError.text = "Server error: Invalid response"
                        btnLogin.isEnabled = true
                        btnLogin.text = "Login"
                    }
                } catch (e: SocketTimeoutException) {
                    Log.e(TAG, "Connection timeout", e)
                    txtPasswordError.text = "Connection timeout. Server might be down."
                    Toast.makeText(
                        this@LoginActivity,
                        "Can't connect to server. Please check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                } catch (e: IOException) {
                    Log.e(TAG, "Network error", e)
                    txtPasswordError.text = "Network error. Please check your connection."
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                } catch (e: Exception) {
                    Log.e(TAG, "Login error", e)
                    txtPasswordError.text = "Login error: ${e.message ?: "Unknown error"}"
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            }
        }

        // Redirect to register page
        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun updateButtonState() {
        val isValid = emailIsGood && passwordIsGood
        btnLogin.isEnabled = isValid
        btnLogin.alpha = if (isValid) 1.0f else 0.5f
    }
}
