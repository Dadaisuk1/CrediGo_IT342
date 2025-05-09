package com.sia.credigo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sia.credigo.R
import com.sia.credigo.fragments.NavbarFragment

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        // Set up navbar
        supportFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, NavbarFragment())
            .commit()

        // Set header title
        findViewById<TextView>(R.id.tv_header_title).text = "About Us"

        // Set up back button
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }
}
