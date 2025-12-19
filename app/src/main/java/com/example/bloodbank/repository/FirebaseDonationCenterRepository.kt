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
import javax.inject.Singleton

@Singleton
class FirebaseDonationCenterRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper
) : DonationCenterRepository {

    override fun getDonationCenters(): Flow<Result<List<DonationCenter>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val centers = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(DonationCenter::class.java)?.apply {
                        centerId = dataSnapshot.key
                    }
                }
                trySend(Result.Success(centers)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        databaseHelper.getDonationCentersReference().addValueEventListener(listener)
        awaitClose { databaseHelper.getDonationCentersReference().removeEventListener(listener) }
    }

    override fun addDonationCenter(center: DonationCenter): Flow<Result<Unit>> = callbackFlow {
        val newCenterRef = databaseHelper.getDonationCentersReference().push()
        val centerId = newCenterRef.key ?: run {
            trySend(Result.Error(Exception("Failed to generate center ID")))
            awaitClose { }
            return@callbackFlow
        }
        center.centerId = centerId

        newCenterRef.setValue(center)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit)).isSuccess
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error"))).isSuccess
                }
                close()
            }
        awaitClose { }
    }

    override fun updateDonationCenter(center: DonationCenter): Flow<Result<Unit>> = callbackFlow {
        val centerId = center.centerId ?: run {
            trySend(Result.Error(Exception("Center ID cannot be null")))
            awaitClose { }
            return@callbackFlow
        }
        databaseHelper.getDonationCenterReference(centerId).setValue(center)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit)).isSuccess
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error"))).isSuccess
                }
                close()
            }
        awaitClose { }
    }

    override fun deleteDonationCenter(centerId: String): Flow<Result<Unit>> = callbackFlow {
        databaseHelper.getDonationCenterReference(centerId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.Success(Unit)).isSuccess
                } else {
                    trySend(Result.Error(task.exception ?: Exception("Unknown error"))).isSuccess
                }
                close()
            }
        awaitClose { }
    }
}