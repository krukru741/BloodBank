package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * LoginActivity - User authentication entry point.
 * Follows MVVM architecture with AuthViewModel.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    
    private val viewModel: AuthViewModel by viewModels()
    
    // Views
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPasswordButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Check if already logged in
        if (viewModel.currentUser.value != null) {
            navigateToMain()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Observe ViewModel
        observeViewModel()
    }
    
    private fun initializeViews() {
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        registerButton = findViewById(R.id.registerButton)
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            
            // Clear previous errors
            emailLayout.error = null
            passwordLayout.error = null
            
            viewModel.login(email, password)
        }
        
        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        
        registerButton.setOnClickListener {
            startActivity(Intent(this, SelectRegistrationActivity::class.java))
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe authentication state
                launch {
                    viewModel.authState.collect { state ->
                        when (state) {
                            is AuthViewModel.AuthState.Authenticated -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToMain()
                            }
                            is AuthViewModel.AuthState.Error -> {
                                // Error is handled by error flow
                            }
                            else -> {
                                // Other states handled elsewhere
                            }
                        }
                    }
                }
                
                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        loginButton.isEnabled = !isLoading
                        loginButton.text = if (isLoading) "Logging in..." else "Login"
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            when {
                                it.contains("email", ignoreCase = true) -> {
                                    emailLayout.error = it
                                }
                                it.contains("password", ignoreCase = true) -> {
                                    passwordLayout.error = it
                                }
                                else -> {
                                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                                }
                            }
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
    
    override fun onBackPressed() {
        // Prevent going back to previous screen
        finishAffinity()
    }
}
