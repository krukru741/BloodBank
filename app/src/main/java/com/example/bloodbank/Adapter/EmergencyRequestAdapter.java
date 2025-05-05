package com.example.bloodbank.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.EmergencyRequestDetailsActivity;
import com.example.bloodbank.Model.EmergencyRequest;
import com.example.bloodbank.R;
import com.example.bloodbank.UserProfileActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyRequestAdapter extends RecyclerView.Adapter<EmergencyRequestAdapter.ViewHolder> {
    private Context context;
    private List<EmergencyRequest> requestList;

    public EmergencyRequestAdapter(Context context, List<EmergencyRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyRequest request = requestList.get(position);

        // Set hospital name
        holder.hospitalNameText.setText(request.getHospitalName());

        // Set patient name
        holder.patientNameText.setText("Patient: " + request.getPatientName());

        // Set blood group and units
        holder.bloodGroupText.setText("Blood Group: " + request.getBloodGroup());
        holder.unitsText.setText("Units: " + request.getUnitsNeeded());

        // Set priority level with color coding
        String priority;
        int priorityColor;
        String priorityLevel = request.getPriorityLevel();
        if (priorityLevel == null) {
            priority = "NORMAL";
            priorityColor = android.graphics.Color.GREEN;
        } else {
            switch (priorityLevel.toUpperCase()) {
                case "CRITICAL":
                    priority = "CRITICAL";
                    priorityColor = android.graphics.Color.RED;
                    break;
                case "URGENT":
                    priority = "URGENT";
                    priorityColor = android.graphics.Color.rgb(255, 165, 0); // Orange
                    break;
                default:
                    priority = "NORMAL";
                    priorityColor = android.graphics.Color.GREEN;
                    break;
            }
        }
        holder.priorityText.setText(priority);
        GradientDrawable background = (GradientDrawable) holder.priorityText.getBackground();
        background.setColor(priorityColor);

        // Format and set timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(request.getTimestamp()));
        holder.timestampText.setText(formattedDate);

        // Set status
        String status = request.getStatus();
        if (status == null) status = "UNKNOWN";
        holder.statusText.setText("Status: " + status);

        // Set click listener for the entire card
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmergencyRequestDetailsActivity.class);
            intent.putExtra("requestId", request.getRequestId());
            context.startActivity(intent);
        });

        // Set click listener for the hospital name to view user profile
        holder.hospitalNameText.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", request.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void updateList(List<EmergencyRequest> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView hospitalNameText;
        private TextView patientNameText;
        private TextView bloodGroupText;
        private TextView unitsText;
        private TextView priorityText;
        private TextView timestampText;
        private TextView statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.emergency_request_card);
            hospitalNameText = itemView.findViewById(R.id.hospital_name);
            patientNameText = itemView.findViewById(R.id.patient_name);
            bloodGroupText = itemView.findViewById(R.id.blood_group);
            unitsText = itemView.findViewById(R.id.units_needed);
            priorityText = itemView.findViewById(R.id.priority_level);
            timestampText = itemView.findViewById(R.id.timestamp);
            statusText = itemView.findViewById(R.id.status);
        }
    }
}