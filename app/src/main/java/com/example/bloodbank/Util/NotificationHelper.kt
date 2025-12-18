package com.example.bloodbank.Util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bloodbank.EmergencyRequestDetailsActivity
import com.example.bloodbank.R
import com.example.bloodbank.ScheduleDonationActivity

/**
 * NotificationHelper - Utility for managing app notifications.
 * Kotlin object for singleton pattern with enum for priority levels.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "emergency_channel"
    private const val CHANNEL_NAME = "Emergency Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for emergency blood requests"
    private const val APPOINTMENT_CHANNEL_ID = "appointment_channel"
    private const val ELIGIBILITY_CHANNEL_ID = "eligibility_channel"
    
    /**
     * Priority levels for emergency notifications.
     */
    enum class Priority(val level: Int, val color: Int) {
        NORMAL(1, Color.GREEN),
        URGENT(2, Color.YELLOW),
        CRITICAL(3, Color.RED)
    }
    
    /**
     * Create all notification channels (Android O+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // Emergency channel
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            
            // Appointment channel
            val appointmentChannel = NotificationChannel(
                APPOINTMENT_CHANNEL_ID,
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming blood donation appointments"
                enableLights(true)
                lightColor = Color.BLUE
            }
            
            // Eligibility channel
            val eligibilityChannel = NotificationChannel(
                ELIGIBILITY_CHANNEL_ID,
                "Donation Eligibility",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about donation eligibility"
                enableLights(true)
                lightColor = Color.GREEN
            }
            
            // Register all channels
            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(appointmentChannel)
            notificationManager.createNotificationChannel(eligibilityChannel)
        }
    }
    
    /**
     * Send emergency blood request notification.
     */
    fun sendEmergencyNotification(
        context: Context,
        title: String,
        message: String,
        requestId: String,
        priorityLevel: Int = 1
    ) {
        val intent = Intent(context, EmergencyRequestDetailsActivity::class.java).apply {
            putExtra("requestId", requestId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
        
        // Set priority based on emergency level
        when (priorityLevel) {
            3 -> notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLights(Color.RED, 3000, 3000)
                .setColor(Color.RED)
            
            2 -> notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLights(Color.YELLOW, 2000, 2000)
                .setColor(Color.YELLOW)
            
            else -> notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLights(Color.GREEN, 1000, 1000)
                .setColor(Color.GREEN)
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Send appointment reminder notification.
     */
    fun sendAppointmentReminder(
        context: Context,
        title: String,
        message: String,
        appointmentId: String
    ) {
        val intent = Intent(context, ScheduleDonationActivity::class.java).apply {
            putExtra("appointmentId", appointmentId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, APPOINTMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setColor(Color.BLUE)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(appointmentId.hashCode(), notificationBuilder.build())
    }
    
    /**
     * Send donation eligibility notification.
     */
    fun sendEligibilityNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, ScheduleDonationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, ELIGIBILITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setColor(Color.GREEN)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }
}
