package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.Model.EmergencyResponse // Assuming this class exists
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseEmergencyRequestRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : EmergencyRequestRepository {

    override fun createEmergencyRequest(request: EmergencyRequest): Flow<Result<Unit>> = callbackFlow {
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

        // Ensure the request object contains the userId for proper querying later
        val updatedRequest = request.copy(
            requestId = requestId,
            requesterId = userId, // Assign the current user as the requester
            timestamp = System.currentTimeMillis(),
            status = "pending", // Default status
            responses = request.responses ?: hashMapOf() // Ensure responses map is initialized
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

    override fun getEmergencyRequestById(requestId: String): Flow<Result<EmergencyRequest?>> = callbackFlow {
        val requestRef = databaseHelper.getEmergencyRequestReference(requestId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val request = snapshot.getValue(EmergencyRequest::class.java)
                    trySend(Result.Success(request))
                } catch (e: Exception) {
                    trySend(Result.Error(e))
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
                close(error.toException())
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
                    trySend(Result.Success(requests.sortedByDescending { it.timestamp ?: 0L })) // Sort by newest first, handling null timestamp
                } catch (e: Exception) {
                    trySend(Result.Error(e))
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
                close(error.toException())
            }
        }
        allRequestsRef.addValueEventListener(listener)
        awaitClose { allRequestsRef.removeEventListener(listener) }
    }

    override fun getEmergencyRequestsByUserId(userId: String): Flow<Result<List<EmergencyRequest>>> = callbackFlow {
        val userRequestsQuery = databaseHelper.getEmergencyRequestsReference()
            .orderByChild("requesterId") // Assuming 'requesterId' field in EmergencyRequest
            .equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val requests = snapshot.children.mapNotNull { it.getValue(EmergencyRequest::class.java) }
                    trySend(Result.Success(requests.sortedByDescending { it.timestamp ?: 0L }))
                } catch (e: Exception) {
                    trySend(Result.Error(e))
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
                close(error.toException())
            }
        }
        userRequestsQuery.addValueEventListener(listener)
        awaitClose { userRequestsQuery.removeEventListener(listener) }
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

    override fun respondToEmergencyRequest(requestId: String, response: EmergencyResponse): Flow<Result<Unit>> = callbackFlow {
        val responsesRef = databaseHelper.getEmergencyRequestResponsesReference(requestId)
        val responseKey = responsesRef.push().key

        if (responseKey == null) {
            trySend(Result.Error(Exception("Failed to generate response ID")))
            close()
            return@callbackFlow
        }

        // Assuming EmergencyResponse has fields like donorId, accepted, timestamp etc.
        // It should be designed to be directly storable in Firebase.
        responsesRef.child(responseKey).setValue(response)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error responding to request")))
                }
                close()
            }
        awaitClose()
    }

    override fun acceptDonorForRequest(requestId: String, donorId: String): Flow<Result<Unit>> = callbackFlow {
        val requestRef = databaseHelper.getEmergencyRequestReference(requestId)
        requestRef.updateChildren(mapOf("acceptedDonorId" to donorId, "status" to "Accepted"))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit))
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error accepting donor for request")))
                }
                close()
            }
        awaitClose()
    }
}