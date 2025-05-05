package com.example.bloodbank.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.bloodbank.EmergencyRequestDetailsActivity;
import com.example.bloodbank.R;
import com.example.bloodbank.ScheduleDonationActivity;

public class NotificationHelper {
        private static final String CHANNEL_ID = "emergency_channel";
        private static final String CHANNEL_NAME = "Emergency Notifications";
        private static final String CHANNEL_DESCRIPTION = "Notifications for emergency blood requests";
        private static final String APPOINTMENT_CHANNEL_ID = "appointment_channel";
        private static final String ELIGIBILITY_CHANNEL_ID = "eligibility_channel";

        public static void createNotificationChannel(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

                        // Emergency channel
                        NotificationChannel emergencyChannel = new NotificationChannel(
                                        CHANNEL_ID,
                                        CHANNEL_NAME,
                                        NotificationManager.IMPORTANCE_HIGH);

                        emergencyChannel.setDescription(CHANNEL_DESCRIPTION);
                        emergencyChannel.enableLights(true);
                        emergencyChannel.setLightColor(Color.RED);
                        emergencyChannel.enableVibration(true);
                        emergencyChannel.setVibrationPattern(new long[] { 0, 1000, 500, 1000 });

                        // Appointment channel
                        NotificationChannel appointmentChannel = new NotificationChannel(
                                        APPOINTMENT_CHANNEL_ID,
                                        "Appointment Reminders",
                                        NotificationManager.IMPORTANCE_DEFAULT);
                        appointmentChannel.setDescription("Reminders for upcoming blood donation appointments");
                        appointmentChannel.enableLights(true);
                        appointmentChannel.setLightColor(Color.BLUE);

                        // Eligibility channel
                        NotificationChannel eligibilityChannel = new NotificationChannel(
                                        ELIGIBILITY_CHANNEL_ID,
                                        "Donation Eligibility",
                                        NotificationManager.IMPORTANCE_DEFAULT);
                        eligibilityChannel.setDescription("Notifications about donation eligibility");
                        eligibilityChannel.enableLights(true);
                        eligibilityChannel.setLightColor(Color.GREEN);

                        // Register all channels
                        notificationManager.createNotificationChannel(emergencyChannel);
                        notificationManager.createNotificationChannel(appointmentChannel);
                        notificationManager.createNotificationChannel(eligibilityChannel);
                }
        }

        public static void sendEmergencyNotification(Context context, String title, String message,
                        String requestId, int priorityLevel) {
                Intent intent = new Intent(context, EmergencyRequestDetailsActivity.class);
                intent.putExtra("requestId", requestId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                intent,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setSound(soundUri)
                                .setContentIntent(pendingIntent);

                // Set priority based on emergency level
                switch (priorityLevel) {
                        case 3: // Critical
                                notificationBuilder
                                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                                .setCategory(NotificationCompat.CATEGORY_ALARM)
                                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                                .setLights(Color.RED, 3000, 3000)
                                                .setColor(Color.RED);
                                break;
                        case 2: // Urgent
                                notificationBuilder
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                                .setLights(Color.YELLOW, 2000, 2000)
                                                .setColor(Color.YELLOW);
                                break;
                        default: // Normal
                                notificationBuilder
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                                .setLights(Color.GREEN, 1000, 1000)
                                                .setColor(Color.GREEN);
                                break;
                }

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                // Generate unique notification ID based on timestamp
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());
        }

        public static void sendAppointmentReminder(Context context, String title, String message,
                        String appointmentId) {
                Intent intent = new Intent(context, ScheduleDonationActivity.class);
                intent.putExtra("appointmentId", appointmentId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                                context,
                                appointmentId.hashCode(),
                                intent,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                                APPOINTMENT_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_calendar)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setContentIntent(pendingIntent)
                                .setColor(Color.BLUE);

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(appointmentId.hashCode(), notificationBuilder.build());
        }

        public static void sendEligibilityNotification(Context context, String title, String message) {
                Intent intent = new Intent(context, ScheduleDonationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                intent,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                                ELIGIBILITY_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_STATUS)
                                .setContentIntent(pendingIntent)
                                .setColor(Color.GREEN);

                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(1, notificationBuilder.build());
        }
}