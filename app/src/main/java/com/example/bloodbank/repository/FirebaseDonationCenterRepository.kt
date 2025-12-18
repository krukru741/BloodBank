package com.example.bloodbank.repository

import com.example.bloodbank.Model.DonationCenter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDonationCenterRepository @Inject constructor(
    private val firestore: FirebaseFirestore // Changed from DatabaseHelper to FirebaseFirestore
) : DonationCenterRepository {

    override fun getAllDonationCenters(): Flow<List<DonationCenter>> = callbackFlow {
        val subscription = firestore.collection("donation_centers")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Order by createdAt
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow with an exception on error
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val centers = querySnapshot.documents.mapNotNull { document ->
                        // Map document to DonationCenter, include ID
                        document.toObject(DonationCenter::class.java)?.apply {
                            id = document.id
                        }
                    }
                    trySend(centers).isSuccess
                } ?: trySend(emptyList()).isSuccess // Send empty list if snapshot is null
            }

        awaitClose {
            subscription.remove() // Remove the Firestore listener when the flow is cancelled
        }
    }

    override fun getDonationCenterById(centerId: String): Flow<DonationCenter?> = callbackFlow {
        val subscription = firestore.collection("donation_centers").document(centerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow with an exception on error
                    return@addSnapshotListener
                }

                snapshot?.let { documentSnapshot ->
                    val center = documentSnapshot.toObject(DonationCenter::class.java)?.apply {
                        id = documentSnapshot.id // Ensure ID is set from document ID
                    }
                    trySend(center).isSuccess
                } ?: trySend(null).isSuccess // Send null if document does not exist
            }

        awaitClose {
            subscription.remove() // Remove the Firestore listener when the flow is cancelled
        }
    }

    override suspend fun addDonationCenter(center: DonationCenter): Result<Unit> {
        return try {
            firestore.collection("donation_centers").add(center).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

**Please manually replace the content of `app/src/main/java/com/example/bloodbank/repository/FirebaseDonationCenterRepository.kt` with the code above.**

Once you've done that, the `FirebaseDonationCenterRepository` will be correctly set up to use Firestore and implement the `addDonationCenter` function. We'll then proceed with creating the `DonationCentersViewModel` and refactoring `DonationCentersActivity.java`.

Since I cannot directly execute the file operations, for future steps, I will provide the necessary code and instructions for you to apply them manually. We'll mark these suggestions as completed once you confirm they've been applied.

Thank you for your understanding and cooperation!