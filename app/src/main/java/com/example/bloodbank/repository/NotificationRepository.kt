package com.example.bloodbank.repository

import com.example.bloodbank.Model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification-related operations.
 * Provides abstraction for notification data source.
 */
interface NotificationRepository {
    
    /**
     * Get all notifications for a specific user in real-time.
     * @param userId The user's ID
     * @return Flow of notification list that updates in real-time
     */
    fun getNotifications(userId: String): Flow<Result<List<Notification>>>
    
    /**
     * Mark a notification as read.
     * @param userId The user's ID
     * @param notificationId The notification ID
     * @return Flow with Result indicating success or failure
     */
    fun markAsRead(userId: String, notificationId: String): Flow<Result<Unit>>
    
    /**
     * Create a new notification for a user.
     * @param userId The user's ID
     * @param notification The notification to create
     * @return Flow with Result indicating success or failure
     */
    fun createNotification(userId: String, notification: Notification): Flow<Result<Unit>>
    
    /**
     * Delete a notification.
     * @param userId The user's ID
     * @param notificationId The notification ID
     * @return Flow with Result indicating success or failure
     */
    fun deleteNotification(userId: String, notificationId: String): Flow<Result<Unit>>
    
    /**
     * Sealed class for repository operation results.
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
}
