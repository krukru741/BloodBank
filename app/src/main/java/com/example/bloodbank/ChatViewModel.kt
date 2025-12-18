package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.Message
import com.example.bloodbank.repository.MessageRepository
import com.example.bloodbank.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ChatActivity.
 * Manages chat messages and sending functionality.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _sendMessageSuccess = MutableStateFlow(false)
    val sendMessageSuccess: StateFlow<Boolean> = _sendMessageSuccess.asStateFlow()
    
    private var otherUserId: String = ""
    
    /**
     * Load messages between current user and another user.
     */
    fun loadMessages(userId: String) {
        otherUserId = userId
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            messageRepository.getMessages(currentUserId, userId).collect { result ->
                when (result) {
                    is MessageRepository.Result.Success -> {
                        _messages.value = result.data
                        _isLoading.value = false
                        _error.value = null
                    }
                    is MessageRepository.Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load messages"
                        _isLoading.value = false
                    }
                    is MessageRepository.Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    /**
     * Send a message to the other user.
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) {
            _error.value = "Message cannot be empty"
            return
        }
        
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            messageRepository.sendMessage(currentUserId, otherUserId, messageText).collect { result ->
                when (result) {
                    is MessageRepository.Result.Success -> {
                        _sendMessageSuccess.value = true
                        _error.value = null
                        // Reset success flag after a short delay
                        _sendMessageSuccess.value = false
                    }
                    is MessageRepository.Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to send message"
                    }
                    is MessageRepository.Result.Loading -> {
                        // Optional: show sending indicator
                    }
                }
            }
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
}
