package com.example.bloodbank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.Result
import com.example.bloodbank.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userType = MutableStateFlow<String?>(null)
    val userType: StateFlow<String?> = _userType.asStateFlow()

    private val _donors = MutableStateFlow<List<User>>(emptyList())
    val donors: StateFlow<List<User>> = _donors.asStateFlow()

    private val _recipients = MutableStateFlow<List<User>>(emptyList())
    val recipients: StateFlow<List<User>> = _recipients.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = userRepository.getCurrentUserUid()
            if (userId != null) {
                // Collect current user details
                userRepository.getUserDetails(userId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> _currentUser.value = result.data
                        is Result.Error -> _error.emit(result.exception.message ?: "Failed to load user details")
                    }
                }

                // Collect user type
                userRepository.getUserType(userId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _userType.value = result.data
                            _isLoading.value = false // Set loading to false after user type is known
                            checkUserTypeAndLoadContent()
                        }
                        is Result.Error -> {
                            _error.emit(result.exception.message ?: "Failed to load user type")
                            _isLoading.value = false
                        }
                    }
                }
            } else {
                // User not logged in, trigger logout or handle appropriately
                _error.emit("User not logged in.")
                _logoutEvent.emit(Unit)
                _isLoading.value = false
            }
        }
    }

    fun checkUserTypeAndLoadContent() {
        viewModelScope.launch {
            _isLoading.value = true
            when (_userType.value) {
                "donor" -> readRecipients() // Donor sees recipients
                "recipient" -> readDonors() // Recipient sees donors
                else -> {
                    // Handle unknown user type or initial state
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun readDonors() {
        userRepository.readDonors().collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    _donors.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _error.emit(result.exception.message ?: "Failed to load donors")
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun readRecipients() {
        userRepository.readRecipients().collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    _recipients.value = result.data ?: emptyList()
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _error.emit(result.exception.message ?: "Failed to load recipients")
                    _isLoading.value = false
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _logoutEvent.emit(Unit)
        }
    }

    fun getCurrentUser(): User? {
        return currentUser.value
    }
}