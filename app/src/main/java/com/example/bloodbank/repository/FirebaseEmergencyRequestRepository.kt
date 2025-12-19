package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.EmergencyRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseEmergencyRequestRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : EmergencyRequestRepository {

    override fun createEmergencyRequest(emergencyRequest: EmergencyRequest): Flow<Result<Unit>> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(Result.Error(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        val requestsRef = databaseHelper.getEmergencyRequestsReference()
        val newRequestRef = requestsRef.push()
        val requestId = newRequestRef.key

        if (requestId == null) {
            trySend(Result.Error(Exception("Failed to generate request ID")))
            close()
            return@callbackFlow
        }

        val updatedRequest = emergencyRequest.copy(
            requestId = requestId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            status = "ACTIVE"
        )

        newRequestRef.setValue(updatedRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error creating emergency request")))
                }
                close()
            }
        awaitClose()
    }

    override fun getEmergencyRequest(requestId: String): Flow<Result<EmergencyRequest?>> = callbackFlow {
        val requestRef = databaseHelper.getEmergencyRequestReference(requestId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val request = snapshot.getValue(EmergencyRequest::class.java)
                    trySend(Result.Success(request))
                } catch (e: Exception) {
                    trySend(Result.Error(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
            }
        }
        requestRef.addValueEventListener(listener)
        awaitClose { requestRef.removeEventListener(listener) }
    }

    override fun getAllEmergencyRequests(): Flow<Result<List<EmergencyRequest>>> = callbackFlow {
        val allRequestsRef = databaseHelper.getEmergencyRequestsReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val requests = snapshot.children.mapNotNull { it.getValue(EmergencyRequest::class.java) }
                    trySend(Result.Success(requests.sortedByDescending { it.timestamp }))
                } catch (e: Exception) {
                    trySend(Result.Error(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
            }
        }
        allRequestsRef.addValueEventListener(listener)
        awaitClose { allRequestsRef.removeEventListener(listener) }
    }

    override fun updateEmergencyRequestStatus(requestId: String, status: String): Flow<Result<Unit>> = callbackFlow {
        val requestRef = databaseHelper.getEmergencyRequestReference(requestId)
        requestRef.updateChildren(mapOf("status" to status))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error updating request status")))
                }
                close()
            }
        awaitClose()
    }

    override fun addResponseToEmergencyRequest(requestId: String, donorId: String, accepted: Boolean): Flow<Result<Unit>> = callbackFlow {
        val responsesRef = databaseHelper.getEmergencyRequestResponsesReference(requestId)
        responsesRef.child(donorId).setValue(accepted)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error adding response")))
                }
                close()
            }
        awaitClose()
    }
}
