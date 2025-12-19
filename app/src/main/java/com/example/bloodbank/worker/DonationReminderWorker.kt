package com.example.bloodbank.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bloodbank.Model.DonationAppointment
import com.example.bloodbank.Model.DonationCenter
import com.example.bloodbank.Util.NotificationHelper
import com.google.firebase.database.FirebaseDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

/**
 * DonationReminderWorker - WorkManager worker for donation reminders.
 * Uses Hilt for dependency injection and Coroutines for async operations.
 */
@HiltWorker
class DonationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val KEY_APPOINTMENT_ID = "appointment_id"
        const val TYPE_APPOINTMENT = "appointment"
        const val TYPE_ELIGIBILITY = "eligibility"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val reminderType = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()
            
            when (reminderType) {
                TYPE_APPOINTMENT -> {
                    val appointmentId = inputData.getString(KEY_APPOINTMENT_ID)
                        ?: return Result.failure()
                    sendAppointmentReminder(appointmentId)
                }
                TYPE_ELIGIBILITY -> {
                    sendEligibilityReminder()
                }
                else -> return Result.failure()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    /**
     * Send appointment reminder notification.
     */
    private suspend fun sendAppointmentReminder(appointmentId: String) {
        val db = FirebaseDatabase.getInstance()
        
        // Get appointment details
        val appointmentSnapshot = db.reference
            .child("appointments")
            .child(appointmentId)
            .get()
            .await()
        
        val appointment = appointmentSnapshot.getValue(DonationAppointment::class.java)
            ?: return
        
        // Only send reminder if appointment is still scheduled
        if (appointment.status != "SCHEDULED") return
        
        // Get donation center details
        val centerId = appointment.centerId ?: return
        val centerSnapshot = db.reference
            .child("donation_centers")
            .child(centerId)
            .get()
            .await()
        
        val center = centerSnapshot.getValue(DonationCenter::class.java)
            ?: return
        
        // Send notification
        val title = "Donation Appointment Tomorrow"
        val message = "You have a blood donation appointment tomorrow at ${appointment.timeSlot} at ${center.name}"
        
        NotificationHelper.sendAppointmentReminder(
            applicationContext,
            title,
            message,
            appointmentId
        )
    }
    
    /**
     * Send eligibility reminder notification.
     */
    private fun sendEligibilityReminder() {
        NotificationHelper.sendEligibilityNotification(
            applicationContext,
            "You're Eligible to Donate!",
            "It's been 56 days since your last donation. You can now schedule your next donation."
        )
    }
}
