package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * SentEmailActivity - Confirmation screen after email verification sent.
 * Simple navigation activity, no ViewModel needed.
 */
class SentEmailActivity : AppCompatActivity() {
    
    private lateinit var backToLoginButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sent_email)
        
        setupButton()
    }
    
    private fun setupButton() {
        backToLoginButton = findViewById(R.id.backToLoginButton)
        backToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
