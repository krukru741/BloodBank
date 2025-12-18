package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * SplashScreenActivity - App entry point with branding.
 * Simple navigation activity, no ViewModel needed.
 */
class SplashScreenActivity : AppCompatActivity() {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        
        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }
    
    private fun navigateToNextScreen() {
        val intent = if (firebaseAuth.currentUser != null) {
            // User is logged in, go to main
            Intent(this, MainActivity::class.java)
        } else {
            // User not logged in, go to login
            Intent(this, SelectRegistrationActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
