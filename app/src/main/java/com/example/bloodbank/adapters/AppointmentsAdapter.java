package com.example.bloodbank.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.R;
import com.example.bloodbank.models.Donation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder> {
    private List<Donation> appointmentsList;
    private Context context;
    private SimpleDateFormat dateFormat;
    private DatabaseReference donationsRef;

    public AppointmentsAdapter(List<Donation> appointmentsList) {
        this.appointmentsList = appointmentsList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        this.donationsRef = FirebaseDatabase.getInstance().getReference().child("donations");
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Donation appointment = appointmentsList.get(position);
        
        // Format date and time
        String dateTime = appointment.getDonationDate();
        holder.dateTimeTextView.setText(dateTime);
        
        // Set center name
        holder.centerTextView.setText(appointment.getDonationCenter());
        
        // Set status with appropriate color
        String status = appointment.getStatus();
        holder.statusTextView.setText(status);
        
        // Set status color based on status
        int statusColor;
        switch (status.toLowerCase()) {
            case "scheduled":
                statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "completed":
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.statusTextView.setTextColor(statusColor);
        
        // Set notes if available
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            holder.notesTextView.setVisibility(View.VISIBLE);
            holder.notesTextView.setText(appointment.getNotes());
        } else {
            holder.notesTextView.setVisibility(View.GONE);
        }
        
        // Show/hide cancel button based on status
        if ("scheduled".equalsIgnoreCase(status)) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> cancelAppointment(appointment));
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }
    
    private void cancelAppointment(Donation appointment) {
        // Update appointment status to cancelled
        donationsRef.child(appointment.getDonationId())
                .child("status")
                .setValue("cancelled")
                .addOnSuccessListener(aVoid -> {
                    // Appointment cancelled successfully
                    // The RecyclerView will update automatically due to the ValueEventListener
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView;
        TextView centerTextView;
        TextView statusTextView;
        TextView notesTextView;
        Button cancelButton;

        AppointmentViewHolder(View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.textViewDateTime);
            centerTextView = itemView.findViewById(R.id.textViewCenter);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            notesTextView = itemView.findViewById(R.id.textViewNotes);
            cancelButton = itemView.findViewById(R.id.buttonCancel);
        }
    }
} 