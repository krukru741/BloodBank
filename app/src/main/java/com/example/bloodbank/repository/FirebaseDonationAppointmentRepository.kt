package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.DonationAppointment
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseDonationAppointmentRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : DonationAppointmentRepository {

    override fun getAppointmentsForUser(userId: String): Flow<List<DonationAppointment>> = callbackFlow {
        val appointmentsRef = databaseHelper.getDonationsReference()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointments = mutableListOf<DonationAppointment>()
                for (appointmentSnapshot in snapshot.children) {
                    val appointment = appointmentSnapshot.getValue(DonationAppointment::class.java)
                    // Filter by donorId if it matches the userId
                    if (appointment != null && appointment.donorId == userId) {
                        appointments.add(appointment)
                    }
                }
                trySend(appointments).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an exception on cancellation
            }
        }

        // Attach listener to the donations node
        appointmentsRef.addValueEventListener(listener)

        // The flow will be cancelled when the collector is no longer active
        // or if an exception occurs.
        awaitClose {
            appointmentsRef.removeEventListener(listener)
        }
    }

    override suspend fun scheduleAppointment(appointment: DonationAppointment): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val newAppointmentRef = databaseHelper.getDonationsReference().push() // Generate a unique key
        val appointmentId = newAppointmentRef.key ?: return@suspendCancellableCoroutine continuation.resume(Result.Error(Exception("Failed to generate appointment ID")))

        val appointmentWithId = appointment.copy(appointmentId = appointmentId)

        newAppointmentRef.setValue(appointmentWithId)
            .addOnSuccessListener {
                continuation.resume(Result.Success(Unit))
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.Error(exception))
            }
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val updates = mapOf("status" to status, "lastUpdated" to System.currentTimeMillis())
        databaseHelper.getDonationReference(firebaseAuth.currentUser?.uid ?: "", appointmentId)
            .updateChildren(updates)
            .addOnSuccessListener {
                continuation.resume(Result.Success(Unit))
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.Error(exception))
            }
    }
}