package com.example.bloodbank.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bloodbank.Model.DonationAppointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * DonationReminderScheduler - Schedules donation reminder work using WorkManager.
 * Modern replacement for AlarmManager-based scheduling.
 */
class DonationReminderScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    
    companion object {
        private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L // 24 hours
        private const val REMINDER_BEFORE_APPOINTMENT = 24 * 60 * 60 * 1000L // 24 hours
        private const val ELIGIBILITY_PERIOD = 56 * DAY_IN_MILLIS // 56 days for whole blood
        
        private const val WORK_TAG_APPOINTMENT = "appointment_reminder"
        private const val WORK_TAG_ELIGIBILITY = "eligibility_reminder"
    }
    
    /**
     * Schedule reminder for next eligible donation date.
     */
    fun scheduleNextDonationReminder(lastDonationDate: Long) {
        val nextEligibleDate = lastDonationDate + ELIGIBILITY_PERIOD
        val delay = nextEligibleDate - System.currentTimeMillis()
        
        if (delay <= 0) return // Already eligible
        
        val inputData = Data.Builder()
            .putString(DonationReminderWorker.KEY_REMINDER_TYPE, DonationReminderWorker.TYPE_ELIGIBILITY)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<DonationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG_ELIGIBILITY)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "eligibility_reminder",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Schedule appointment reminder 24 hours before appointment.
     */
    fun scheduleAppointmentReminder(appointment: DonationAppointment) {
        val reminderTime = appointment.appointmentDate - REMINDER_BEFORE_APPOINTMENT
        val delay = reminderTime - System.currentTimeMillis()
        
        if (delay <= 0) return // Appointment is too soon
        
        val inputData = Data.Builder()
            .putString(DonationReminderWorker.KEY_REMINDER_TYPE, DonationReminderWorker.TYPE_APPOINTMENT)
            .putString(DonationReminderWorker.KEY_APPOINTMENT_ID, appointment.appointmentId)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<DonationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG_APPOINTMENT)
            .addTag("appointment_${appointment.appointmentId}")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "appointment_reminder_${appointment.appointmentId}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Check eligibility and schedule reminder if needed.
     */
    suspend fun checkAndUpdateEligibility() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        val appointmentsSnapshot = database.reference
            .child("appointments")
            .orderByChild("donorId")
            .equalTo(userId)
            .get()
            .await()
        
        var lastDonationDate = 0L
        
        appointmentsSnapshot.children.forEach { snapshot ->
            val appointment = snapshot.getValue(DonationAppointment::class.java)
            if (appointment?.status == "COMPLETED" && appointment.appointmentDate > lastDonationDate) {
                lastDonationDate = appointment.appointmentDate
            }
        }
        
        if (lastDonationDate > 0) {
            val nextEligibleDate = lastDonationDate + ELIGIBILITY_PERIOD
            
            if (System.currentTimeMillis() >= nextEligibleDate) {
                // User is eligible now - send notification immediately
                val inputData = Data.Builder()
                    .putString(DonationReminderWorker.KEY_REMINDER_TYPE, DonationReminderWorker.TYPE_ELIGIBILITY)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<DonationReminderWorker>()
                    .setInputData(inputData)
                    .build()
                
                workManager.enqueue(workRequest)
            } else {
                // Schedule reminder for when user becomes eligible
                scheduleNextDonationReminder(lastDonationDate)
            }
        }
    }
    
    /**
     * Cancel appointment reminder.
     */
    fun cancelAppointmentReminder(appointmentId: String) {
        workManager.cancelAllWorkByTag("appointment_$appointmentId")
    }
    
    /**
     * Cancel all reminders.
     */
    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(WORK_TAG_APPOINTMENT)
        workManager.cancelAllWorkByTag(WORK_TAG_ELIGIBILITY)
    }
}
