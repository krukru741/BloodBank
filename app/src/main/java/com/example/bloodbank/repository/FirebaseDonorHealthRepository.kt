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
import javax.inject.Inject

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

    override fun updateDonorHealth(donorId: String, donorHealth: DonorHealth): Flow<Result<Unit>> = callbackFlow {
        val healthRef = databaseHelper.getUserHealthReference(donorId)
        healthRef.setValue(donorHealth)
            .addOnSuccessListener {
                trySend(Result.Success(Unit))
                close()
            }
            .addOnFailureListener { e ->
                trySend(Result.Error(e))
                close()
            }
        awaitClose { }
    }

    override fun createDonorHealth(donorId: String, donorHealth: DonorHealth): Flow<Result<Unit>> = callbackFlow {
        val healthRef = databaseHelper.getUserHealthReference(donorId)
        healthRef.setValue(donorHealth)
            .addOnSuccessListener {
                trySend(Result.Success(Unit))
                close()
            }
            .addOnFailureListener { e ->
                trySend(Result.Error(e))
                close()
            }
        awaitClose { }
    }
}
