package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ProfileActivity.
 * Manages user profile data and navigation to update screen.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _navigateToUpdate = MutableStateFlow(false)
    val navigateToUpdate: StateFlow<Boolean> = _navigateToUpdate.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    /**
     * Load the current user's profile data.
     */
    fun loadUserProfile() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUserDetails(currentUserId)
                .catch { e ->
                    _error.value = e.message ?: "Failed to load profile"
                    _isLoading.value = false
                }
                .collect { user ->
                    _user.value = user
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }
    
    /**
     * Navigate to update profile screen.
     */
    fun onUpdateProfileClick() {
        _navigateToUpdate.value = true
    }
    
    /**
     * Reset navigation flag after navigation is complete.
     */
    fun onNavigationComplete() {
        _navigateToUpdate.value = false
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh profile data.
     */
    fun refresh() {
        loadUserProfile()
    }
}
