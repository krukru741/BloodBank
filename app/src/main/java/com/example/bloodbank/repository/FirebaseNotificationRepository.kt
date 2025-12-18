package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of NotificationRepository.
 * Handles real-time notification synchronization using Kotlin Flows.
 */
@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : NotificationRepository {
    
    override fun getNotifications(userId: String): Flow<NotificationRepository.Result<List<Notification>>> = callbackFlow {
        val notificationsRef = databaseHelper.database.getReference("notifications").child(userId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val notifications = mutableListOf<Notification>()
                    
                    for (dataSnapshot in snapshot.children) {
                        val notification = dataSnapshot.getValue(Notification::class.java)
                        notification?.let {
                            notifications.add(it)
                        }
                    }
                    
                    // Sort by time (most recent first)
                    notifications.sortByDescending { it.time }
                    
                    trySend(NotificationRepository.Result.Success(notifications))
                } catch (e: Exception) {
                    trySend(NotificationRepository.Result.Error(e))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(NotificationRepository.Result.Error(error.toException()))
            }
        }
        
        notificationsRef.addValueEventListener(listener)
        
        awaitClose {
            notificationsRef.removeEventListener(listener)
        }
    }
    
    override fun markAsRead(userId: String, notificationId: String): Flow<NotificationRepository.Result<Unit>> = flow {
        try {
            emit(NotificationRepository.Result.Loading)
            
            val notificationRef = databaseHelper.database
                .getReference("notifications")
                .child(userId)
                .child(notificationId)
            
            notificationRef.child("isRead").setValue(true).await()
            
            emit(NotificationRepository.Result.Success(Unit))
        } catch (e: Exception) {
            emit(NotificationRepository.Result.Error(e))
        }
    }
    
    override fun createNotification(
        userId: String,
        notification: Notification
    ): Flow<NotificationRepository.Result<Unit>> = flow {
        try {
            emit(NotificationRepository.Result.Loading)
            
            val notificationsRef = databaseHelper.database.getReference("notifications").child(userId)
            val pushId = notificationsRef.push().key
                ?: throw Exception("Failed to generate notification ID")
            
            notificationsRef.child(pushId).setValue(notification).await()
            
            emit(NotificationRepository.Result.Success(Unit))
        } catch (e: Exception) {
            emit(NotificationRepository.Result.Error(e))
        }
    }
    
    override fun deleteNotification(
        userId: String,
        notificationId: String
    ): Flow<NotificationRepository.Result<Unit>> = flow {
        try {
            emit(NotificationRepository.Result.Loading)
            
            val notificationRef = databaseHelper.database
                .getReference("notifications")
                .child(userId)
                .child(notificationId)
            
            notificationRef.removeValue().await()
            
            emit(NotificationRepository.Result.Success(Unit))
        } catch (e: Exception) {
            emit(NotificationRepository.Result.Error(e))
        }
    }
}
