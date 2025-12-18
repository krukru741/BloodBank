package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.Message
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
 * Firebase implementation of MessageRepository.
 * Handles real-time message synchronization using Kotlin Flows.
 */
@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : MessageRepository {
    
    override fun getMessages(
        currentUserId: String,
        otherUserId: String
    ): Flow<MessageRepository.Result<List<Message>>> = callbackFlow {
        val chatRef = databaseHelper.database.getReference("Chats")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val messages = mutableListOf<Message>()
                    
                    for (dataSnapshot in snapshot.children) {
                        val message = dataSnapshot.getValue(Message::class.java)
                        message?.let {
                            // Filter messages between the two users
                            if ((it.senderId == currentUserId && it.receiverId == otherUserId) ||
                                (it.senderId == otherUserId && it.receiverId == currentUserId)) {
                                messages.add(it)
                            }
                        }
                    }
                    
                    // Sort by timestamp
                    messages.sortBy { it.timestamp }
                    
                    trySend(MessageRepository.Result.Success(messages))
                } catch (e: Exception) {
                    trySend(MessageRepository.Result.Error(e))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(MessageRepository.Result.Error(error.toException()))
            }
        }
        
        chatRef.addValueEventListener(listener)
        
        awaitClose {
            chatRef.removeEventListener(listener)
        }
    }
    
    override fun sendMessage(
        senderId: String,
        receiverId: String,
        messageText: String
    ): Flow<MessageRepository.Result<Unit>> = flow {
        try {
            emit(MessageRepository.Result.Loading)
            
            val chatRef = databaseHelper.database.getReference("Chats")
            val pushId = chatRef.push().key
                ?: throw Exception("Failed to generate message ID")
            
            val messageMap = hashMapOf<String, Any>(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to messageText,
                "timestamp" to System.currentTimeMillis()
            )
            
            chatRef.child(pushId).setValue(messageMap).await()
            
            emit(MessageRepository.Result.Success(Unit))
        } catch (e: Exception) {
            emit(MessageRepository.Result.Error(e))
        }
    }
}
