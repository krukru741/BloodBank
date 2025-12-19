package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * ForgotPasswordActivity - Password recovery screen.
 * Follows MVVM architecture with AuthViewModel.
 */
@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    
    private val viewModel: AuthViewModel by viewModels()
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var backToLoginButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Setup click listeners
        setupClickListeners()
        
        // Observe ViewModel
        observeViewModel()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        emailLayout = findViewById(R.id.emailLayout)
        emailInput = findViewById(R.id.forgotPass)
        sendButton = findViewById(R.id.resetButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Forgot Password"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            
            // Clear previous errors
            emailLayout.error = null
            
            viewModel.sendPasswordResetEmail(email)
        }
        
        backToLoginButton.setOnClickListener {
            finish()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe authentication state
                launch {
                    viewModel.authState.collect { state ->
                        when (state) {
                            is AuthViewModel.AuthState.PasswordResetSent -> {
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "Password reset email sent! Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
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
                        sendButton.isEnabled = !isLoading
                        sendButton.text = if (isLoading) "Sending..." else "Reset Password"
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            emailLayout.error = it
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
