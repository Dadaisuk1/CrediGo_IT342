package com.sia.credigo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sia.credigo.R
import com.sia.credigo.fragments.NavbarFragment

class ContactUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        // Set up navbar
        val commit = supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()

        // Set header title
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = "Contact Us"

        // Set up back button
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }
}
