package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseMessageRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper
) : MessageRepository {

    override fun getMessages(currentUserId: String, otherUserId: String): Flow<MessageRepository.Result<List<Message>>> = callbackFlow {
        val messagesRef = databaseHelper.getDonationsReference().child("messages")
        val chatRoomId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.child(chatRoomId).children.mapNotNull { it.getValue(Message::class.java) }
                trySend(MessageRepository.Result.Success(messages))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(MessageRepository.Result.Error(error.toException()))
            }
        }
        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    override fun sendMessage(senderId: String, receiverId: String, messageText: String): Flow<MessageRepository.Result<Unit>> = callbackFlow {
        val messagesRef = databaseHelper.getDonationsReference().child("messages")
        val chatRoomId = if (senderId < receiverId) "${senderId}_${receiverId}" else "${receiverId}_${senderId}"
        
        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        
        messagesRef.child(chatRoomId).push().setValue(message)
            .addOnSuccessListener { 
                trySend(MessageRepository.Result.Success(Unit))
                close() 
            }
            .addOnFailureListener { 
                trySend(MessageRepository.Result.Error(it))
                close() 
            }
        awaitClose()
    }
}
