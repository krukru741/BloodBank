package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.DonationAppointment
import com.example.bloodbank.repository.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

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
                    if (appointment != null && appointment.donorId == userId) {
                        appointments.add(appointment)
                    }
                }
                trySend(appointments).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        appointmentsRef.addValueEventListener(listener)
        awaitClose {
            appointmentsRef.removeEventListener(listener)
        }
    }

    override fun scheduleAppointment(appointment: DonationAppointment): Flow<Result<Unit>> = callbackFlow {
        val newAppointmentRef = databaseHelper.getDonationsReference().push()
        val appointmentId = newAppointmentRef.key ?: run {
            trySend(Result.Error(Exception("Failed to generate appointment ID")))
            close()
            return@callbackFlow
        }

        val appointmentWithId = appointment.copy(appointmentId = appointmentId)

        newAppointmentRef.setValue(appointmentWithId)
            .addOnSuccessListener {
                trySend(Result.Success(Unit))
                close()
            }
            .addOnFailureListener { exception ->
                trySend(Result.Error(exception))
                close()
            }
        awaitClose { }
    }

    override fun updateAppointmentStatus(appointmentId: String, status: String): Flow<Result<Unit>> = callbackFlow {
        val updates = mapOf("status" to status, "lastUpdated" to System.currentTimeMillis())
        databaseHelper.getDonationsReference().child(appointmentId)
            .updateChildren(updates)
            .addOnSuccessListener {
                trySend(Result.Success(Unit))
                close()
            }
            .addOnFailureListener { exception ->
                trySend(Result.Error(exception))
                close()
            }
        awaitClose { }
    }
}
