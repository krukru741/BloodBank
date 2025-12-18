package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VerifyEmailActivity - Email verification screen.
 * Follows MVVM architecture with AuthViewModel.
 */
@AndroidEntryPoint
class VerifyEmailActivity : AppCompatActivity() {
    
    private val viewModel: AuthViewModel by viewModels()
    
    // Views
    private lateinit var verifyMsg: TextView
    private lateinit var verifySubtitle: TextView
    private lateinit var verifyBtn: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)
        
        // Initialize views
        initializeViews()
        
        // Check if email is already verified
        checkEmailVerification()
        
        // Setup click listeners
        setupClickListeners()
        
        // Observe ViewModel
        observeViewModel()
    }
    
    private fun initializeViews() {
        verifyMsg = findViewById(R.id.verifyMsg)
        verifySubtitle = findViewById(R.id.verifySubtitle)
        verifyBtn = findViewById(R.id.verifyBtn)
    }
    
    private fun checkEmailVerification() {
        if (viewModel.checkEmailVerification()) {
            navigateToMain()
        } else {
            showVerificationUI()
        }
    }
    
    private fun showVerificationUI() {
        verifyMsg.visibility = View.VISIBLE
        verifySubtitle.visibility = View.VISIBLE
        verifyBtn.visibility = View.VISIBLE
    }
    
    private fun setupClickListeners() {
        verifyBtn.setOnClickListener {
            viewModel.sendEmailVerification()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe authentication state
                launch {
                    viewModel.authState.collect { state ->
                        when (state) {
                            is AuthViewModel.AuthState.EmailVerificationSent -> {
                                Toast.makeText(
                                    this@VerifyEmailActivity,
                                    "Verification email has been sent. Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navigateToMain()
                            }
                            is AuthViewModel.AuthState.Error -> {
                                // Error is handled by error flow
                            }
                            else -> {
                                // Other states not relevant here
                            }
                        }
                    }
                }
                
                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        verifyBtn.isEnabled = !isLoading
                        verifyBtn.text = if (isLoading) "Sending..." else "Send Verification Email"
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@VerifyEmailActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
