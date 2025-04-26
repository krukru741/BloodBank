package com.example.bloodbank.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.bloodbank.Model.DonationAppointment;
import com.example.bloodbank.Receiver.DonationReminderReceiver;
import com.example.bloodbank.Util.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class DonationReminderService {
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000; // 24 hours
    private static final long REMINDER_BEFORE_APPOINTMENT = 24 * 60 * 60 * 1000; // 24 hours
    private static final long ELIGIBILITY_PERIOD = 56 * DAY_IN_MILLIS; // 56 days for whole blood donation

    private Context context;
    private DatabaseReference appointmentsRef;
    private String userId;

    public DonationReminderService(Context context) {
        this.context = context;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");
    }

    public void scheduleNextDonationReminder(long lastDonationDate) {
        // Calculate next eligible donation date
        long nextEligibleDate = lastDonationDate + ELIGIBILITY_PERIOD;

        // Schedule reminder for next eligible donation
        Intent intent = new Intent(context, DonationReminderReceiver.class);
        intent.putExtra("type", "eligibility");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextEligibleDate,
                pendingIntent);
    }

    public void scheduleAppointmentReminder(DonationAppointment appointment) {
        // Schedule reminder 24 hours before appointment
        long reminderTime = appointment.getAppointmentDate() - REMINDER_BEFORE_APPOINTMENT;

        if (reminderTime > System.currentTimeMillis()) {
            Intent intent = new Intent(context, DonationReminderReceiver.class);
            intent.putExtra("type", "appointment");
            intent.putExtra("appointmentId", appointment.getAppointmentId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appointment.getAppointmentId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent);
        }
    }

    public void checkAndUpdateEligibility() {
        Query query = appointmentsRef.orderByChild("donorId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long lastDonationDate = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DonationAppointment appointment = snapshot.getValue(DonationAppointment.class);
                    if (appointment != null &&
                            appointment.getStatus().equals("COMPLETED") &&
                            appointment.getAppointmentDate() > lastDonationDate) {
                        lastDonationDate = appointment.getAppointmentDate();
                    }
                }

                if (lastDonationDate > 0) {
                    long nextEligibleDate = lastDonationDate + ELIGIBILITY_PERIOD;
                    if (System.currentTimeMillis() >= nextEligibleDate) {
                        // User is eligible to donate
                        NotificationHelper.sendEligibilityNotification(
                                context,
                                "You're Eligible to Donate!",
                                "It's been 56 days since your last donation. You can now schedule your next donation.");
                    } else {
                        // Schedule reminder for when user becomes eligible
                        scheduleNextDonationReminder(lastDonationDate);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void cancelReminder(String appointmentId) {
        Intent intent = new Intent(context, DonationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                appointmentId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}