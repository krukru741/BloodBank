package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.DonorHealth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDonorHealthRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : DonorHealthRepository {

    override fun getDonorHealth(donorId: String): Flow<DonorHealth?> = callbackFlow {
        val healthRef = databaseHelper.getUserHealthReference(donorId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val donorHealth = snapshot.getValue(DonorHealth::class.java)
                    trySend(donorHealth).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        healthRef.addValueEventListener(listener)
        awaitClose { healthRef.removeEventListener(listener) }
    }

    override suspend fun updateDonorHealth(donorId: String, donorHealth: DonorHealth): Result<Unit> = suspendCoroutine { continuation ->
        val healthRef = databaseHelper.getUserHealthReference(donorId)
        healthRef.setValue(donorHealth)
            .addOnSuccessListener {
                continuation.resume(Result.Success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.Error(e))
            }
    }

    override suspend fun createDonorHealth(donorId: String, donorHealth: DonorHealth): Result<Unit> = suspendCoroutine { continuation ->
        val healthRef = databaseHelper.getUserHealthReference(donorId)
        healthRef.setValue(donorHealth)
            .addOnSuccessListener {
                continuation.resume(Result.Success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.Error(e))
            }
    }
}