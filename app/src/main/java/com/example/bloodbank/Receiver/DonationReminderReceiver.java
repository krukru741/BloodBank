package com.example.bloodbank.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bloodbank.Model.DonationAppointment;
import com.example.bloodbank.Model.DonationCenter;
import com.example.bloodbank.Util.NotificationHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DonationReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");

        if ("appointment".equals(type)) {
            String appointmentId = intent.getStringExtra("appointmentId");
            sendAppointmentReminder(context, appointmentId);
        } else if ("eligibility".equals(type)) {
            NotificationHelper.sendEligibilityNotification(
                    context,
                    "You're Eligible to Donate!",
                    "It's been 56 days since your last donation. You can now schedule your next donation.");
        }
    }

    private void sendAppointmentReminder(Context context, String appointmentId) {
        DatabaseReference appointmentRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("appointments")
                .child(appointmentId);

        appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot appointmentSnapshot) {
                if (appointmentSnapshot.exists()) {
                    DonationAppointment appointment = appointmentSnapshot.getValue(DonationAppointment.class);
                    if (appointment != null && "SCHEDULED".equals(appointment.getStatus())) {
                        // Get donation center details
                        DatabaseReference centerRef = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("donation_centers")
                                .child(appointment.getCenterId());

                        centerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot centerSnapshot) {
                                if (centerSnapshot.exists()) {
                                    DonationCenter center = centerSnapshot.getValue(DonationCenter.class);
                                    if (center != null) {
                                        String title = "Donation Appointment Tomorrow";
                                        String message = "You have a blood donation appointment tomorrow at " +
                                                appointment.getTimeSlot() + " at " + center.getName();

                                        NotificationHelper.sendAppointmentReminder(
                                                context,
                                                title,
                                                message,
                                                appointmentId);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}