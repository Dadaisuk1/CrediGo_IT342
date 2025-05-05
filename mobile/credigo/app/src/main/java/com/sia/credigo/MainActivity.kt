package com.sia.credigo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sia.credigo.app.CredigoApp
//test gh
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            val app = application as CredigoApp
            if (app.sessionManager.isLoggedIn()) {
                // User is already logged in, redirect to home
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return@postDelayed
            }

            // Switch to main layout
            setContentView(R.layout.activity_main)

            supportActionBar?.show()

            val btnLogin = findViewById<Button>(R.id.btn_login)
            val btnRegister = findViewById<Button>(R.id.btn_register)

            btnLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            btnRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }, 2000)
    }
}