package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * SelectRegistrationActivity - User type selection screen.
 * Simple navigation activity, no ViewModel needed.
 */
class SelectRegistrationActivity : AppCompatActivity() {
    
    private lateinit var donorButton: MaterialButton
    private lateinit var recipientButton: MaterialButton
    private lateinit var backButton: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_registration)
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
    }
    
    private fun initializeViews() {
        donorButton = findViewById(R.id.donorButton)
        recipientButton = findViewById(R.id.recipientButton)
        backButton = findViewById(R.id.backButton)
    }
    
    private fun setupClickListeners() {
        donorButton.setOnClickListener {
            startActivity(Intent(this, DonorRegistrationActivity::class.java))
        }
        
        recipientButton.setOnClickListener {
            startActivity(Intent(this, RecipientRegistrationActivity::class.java))
        }
        
        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    
    override fun onBackPressed() {
        // Exit app when back is pressed on selection screen
        finishAffinity()
    }
}
