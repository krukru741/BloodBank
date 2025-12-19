package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.DonationCenter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseDonationCenterRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper
) : DonationCenterRepository {

    override fun getDonationCenters(): Flow<Result<List<DonationCenter>>> = callbackFlow {
        val centersRef = databaseHelper.getDonationCentersReference()
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val centers = snapshot.children.mapNotNull { it.getValue(DonationCenter::class.java) }
                trySend(Result.Success(centers))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.toException()))
            }
        }
        centersRef.addValueEventListener(listener)
        awaitClose { centersRef.removeEventListener(listener) }
    }

    override fun addDonationCenter(center: DonationCenter): Flow<Result<Unit>> = callbackFlow {
        val newCenterRef = databaseHelper.getDonationCentersReference().push()
        val centerId = newCenterRef.key ?: run {
            trySend(Result.Error(Exception("Failed to generate center ID")))
            close()
            return@callbackFlow
        }
        newCenterRef.setValue(center.copy(centerId = centerId))
            .addOnSuccessListener { 
                trySend(Result.Success(Unit))
                close() 
            }
            .addOnFailureListener { 
                trySend(Result.Error(it))
                close() 
            }
        awaitClose()
    }

    override fun updateDonationCenter(center: DonationCenter): Flow<Result<Unit>> = callbackFlow {
        databaseHelper.getDonationCenterReference(center.centerId ?: "").setValue(center)
            .addOnSuccessListener { 
                trySend(Result.Success(Unit))
                close() 
            }
            .addOnFailureListener { 
                trySend(Result.Error(it))
                close() 
            }
        awaitClose()
    }

    override fun deleteDonationCenter(centerId: String): Flow<Result<Unit>> = callbackFlow {
        databaseHelper.getDonationCenterReference(centerId).removeValue()
            .addOnSuccessListener { 
                trySend(Result.Success(Unit))
                close() 
            }
            .addOnFailureListener { trySend(Result.Error(it)); close() }
        awaitClose()
    }
}
