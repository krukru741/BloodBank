package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.Notification
import com.example.bloodbank.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for NotificationsActivity.
 * Manages notification list and read/unread states.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    /**
     * Load notifications for the current user.
     */
    fun loadNotifications() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            notificationRepository.getNotifications(currentUserId).collect { result ->
                when (result) {
                    is NotificationRepository.Result.Success -> {
                        _notifications.value = result.data
                        _unreadCount.value = result.data.count { !it.isRead }
                        _isLoading.value = false
                        _error.value = null
                    }
                    is NotificationRepository.Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to load notifications"
                        _isLoading.value = false
                    }
                    is NotificationRepository.Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    /**
     * Mark a notification as read.
     */
    fun markAsRead(notificationId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            notificationRepository.markAsRead(currentUserId, notificationId).collect { result ->
                when (result) {
                    is NotificationRepository.Result.Success -> {
                        // Success - the real-time listener will update the list
                        _error.value = null
                    }
                    is NotificationRepository.Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to mark as read"
                    }
                    is NotificationRepository.Result.Loading -> {
                        // Optional: show loading indicator
                    }
                }
            }
        }
    }
    
    /**
     * Delete a notification.
     */
    fun deleteNotification(notificationId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            notificationRepository.deleteNotification(currentUserId, notificationId).collect { result ->
                when (result) {
                    is NotificationRepository.Result.Success -> {
                        // Success - the real-time listener will update the list
                        _error.value = null
                    }
                    is NotificationRepository.Result.Error -> {
                        _error.value = result.exception.message ?: "Failed to delete notification"
                    }
                    is NotificationRepository.Result.Loading -> {
                        // Optional: show loading indicator
                    }
                }
            }
        }
    }
    
    /**
     * Get only unread notifications.
     */
    fun getUnreadNotifications(): List<Notification> {
        return _notifications.value.filter { !it.isRead }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
}
