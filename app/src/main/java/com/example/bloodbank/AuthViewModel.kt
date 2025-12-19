package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * AuthViewModel - Centralized authentication logic for the entire app.
 * Handles login, registration, password recovery, and email verification.
 * 
 * Following ultrathink philosophy: One source of truth for authentication state.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Listen to auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }
    
    /**
     * Login with email and password.
     */
    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                
                if (result.user != null) {
                    _authState.value = AuthState.Authenticated(result.user!!)
                    _currentUser.value = result.user
                } else {
                    _authState.value = AuthState.Error("Login failed")
                    _error.value = "Login failed"
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Register a new user.
     */
    fun register(user: User, password: String) {
        if (!validateRegistrationInput(user, password)) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Create Firebase Auth account
                val result = firebaseAuth.createUserWithEmailAndPassword(user.email ?: "", password).await()
                
                if (result.user != null) {
                    // Save user data to database
                    val userWithId = user.copy(id = result.user!!.uid)
                    userRepository.addUser(userWithId).collect { repoResult ->
                        when (repoResult) {
                            is com.example.bloodbank.repository.Result.Success -> {
                                _authState.value = AuthState.Authenticated(result.user!!)
                                _currentUser.value = result.user
                            }
                            is com.example.bloodbank.repository.Result.Error -> {
                                _authState.value = AuthState.Error(repoResult.exception.message ?: "Failed to save user data")
                                _error.value = repoResult.exception.message
                            }
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                    _error.value = "Registration failed"
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send password reset email.
     */
    fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Please enter a valid email address"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                firebaseAuth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.PasswordResetSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
                _error.value = e.message ?: "Failed to send reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send email verification.
     */
    fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            _error.value = "No user logged in"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                user.sendEmailVerification().await()
                _authState.value = AuthState.EmailVerificationSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send verification email")
                _error.value = e.message ?: "Failed to send verification email"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Check if email is verified.
     */
    fun checkEmailVerification(): Boolean {
        firebaseAuth.currentUser?.reload()
        return firebaseAuth.currentUser?.isEmailVerified ?: false
    }
    
    /**
     * Logout current user.
     */
    fun logout() {
        firebaseAuth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
    }
    
    /**
     * Reset auth state to idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
    
    // Validation helpers
    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                _error.value = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _error.value = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                _error.value = "Password is required"
                false
            }
            password.length < 6 -> {
                _error.value = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }
    
    private fun validateRegistrationInput(user: User, password: String): Boolean {
        return when {
            user.name?.isEmpty() != false -> {
                _error.value = "Name is required"
                false
            }
            user.email?.isEmpty() != false -> {
                _error.value = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(user.email ?: "").matches() -> {
                _error.value = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                _error.value = "Password is required"
                false
            }
            password.length < 6 -> {
                _error.value = "Password must be at least 6 characters"
                false
            }
            user.bloodGroup?.isEmpty() != false -> {
                _error.value = "Blood group is required"
                false
            }
            else -> true
        }
    }
    
    /**
     * Sealed class representing authentication states.
     */
    sealed class AuthState {
        object Idle : AuthState()
        object Unauthenticated : AuthState()
        data class Authenticated(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
        object PasswordResetSent : AuthState()
        object EmailVerificationSent : AuthState()
    }
}
