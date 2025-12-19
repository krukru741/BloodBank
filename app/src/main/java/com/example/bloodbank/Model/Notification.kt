package com.example.bloodbank.Model

import androidx.annotation.Keep

/**
 * Notification data model for user notifications.
 * Represents a notification sent to a user.
 */
@Keep
data class Notification(
    val userId: String = "",
    val message: String = "",
    val time: String = "",
    val isRead: Boolean = false,
    val id: String = ""
)
