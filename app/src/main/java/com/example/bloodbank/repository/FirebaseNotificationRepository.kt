package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseNotificationRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<NotificationRepository.Result<List<Notification>>> = callbackFlow {
        val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = snapshot.children.mapNotNull { it.getValue(Notification::class.java) }
                trySend(NotificationRepository.Result.Success(notifications))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(NotificationRepository.Result.Error(error.toException()))
            }
        }
        notificationsRef.addValueEventListener(listener)
        awaitClose { notificationsRef.removeEventListener(listener) }
    }

    override fun markAsRead(userId: String, notificationId: String): Flow<NotificationRepository.Result<Unit>> = callbackFlow {
        FirebaseDatabase.getInstance().getReference("notifications").child(userId).child(notificationId)
            .child("read").setValue(true)
            .addOnSuccessListener { trySend(NotificationRepository.Result.Success(Unit)); close() }
            .addOnFailureListener { trySend(NotificationRepository.Result.Error(it)); close() }
        awaitClose()
    }

    override fun createNotification(userId: String, notification: Notification): Flow<NotificationRepository.Result<Unit>> = callbackFlow {
        val ref = FirebaseDatabase.getInstance().getReference("notifications").child(userId).push()
        val id = ref.key
        ref.setValue(notification.copy(id = id ?: ""))
            .addOnSuccessListener { trySend(NotificationRepository.Result.Success(Unit)); close() }
            .addOnFailureListener { trySend(NotificationRepository.Result.Error(it)); close() }
        awaitClose()
    }

    override fun deleteNotification(userId: String, notificationId: String): Flow<NotificationRepository.Result<Unit>> = callbackFlow {
        FirebaseDatabase.getInstance().getReference("notifications").child(userId).child(notificationId)
            .removeValue()
            .addOnSuccessListener { trySend(NotificationRepository.Result.Success(Unit)); close() }
            .addOnFailureListener { trySend(NotificationRepository.Result.Error(it)); close() }
        awaitClose()
    }
}
