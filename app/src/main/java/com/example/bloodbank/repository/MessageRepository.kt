package com.example.bloodbank.repository

import com.example.bloodbank.Model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for message-related operations.
 * Provides abstraction for chat messaging data source.
 */
interface MessageRepository {
    
    /**
     * Get messages between two users in real-time.
     * @param currentUserId The current user's ID
     * @param otherUserId The other user's ID
     * @return Flow of message list that updates in real-time
     */
    fun getMessages(currentUserId: String, otherUserId: String): Flow<Result<List<Message>>>
    
    /**
     * Send a message to another user.
     * @param senderId The sender's user ID
     * @param receiverId The receiver's user ID
     * @param messageText The message content
     * @return Flow with Result indicating success or failure
     */
    fun sendMessage(senderId: String, receiverId: String, messageText: String): Flow<Result<Unit>>
    
    /**
     * Sealed class for repository operation results.
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
}
