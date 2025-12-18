package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * CategorySelectedActivity - User type selection confirmation.
 * Simple navigation activity, no ViewModel needed.
 */
class CategorySelectedActivity : AppCompatActivity() {
    
    private lateinit var continueButton: MaterialButton
    private var userType: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selected)
        
        // Get user type from intent
        userType = intent.getStringExtra("userType") ?: ""
        
        setupButton()
    }
    
    private fun setupButton() {
        continueButton = findViewById(R.id.continueButton)
        continueButton.setOnClickListener {
            val intent = when (userType) {
                "donor" -> Intent(this, DonorRegistrationActivity::class.java)
                "recipient" -> Intent(this, RecipientRegistrationActivity::class.java)
                else -> Intent(this, SelectRegistrationActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}
