package com.example.bloodbank.Model

import androidx.annotation.Keep

/**
 * Message data model for chat functionality.
 * Represents a single message exchanged between two users.
 */
@Keep
data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", 0L)
}
