package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.ServerValue;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EmergencyRequestDetailsActivity extends AppCompatActivity {

    private TextView hospitalNameText, bloodGroupText, unitsNeededText, emergencyDetailsText, statusText;
    private Button acceptRequestBtn, rejectRequestBtn;
    private String requestId;
    private DatabaseReference requestRef, responseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request_details);

        // Initialize views
        hospitalNameText = findViewById(R.id.hospitalNameText);
        bloodGroupText = findViewById(R.id.bloodGroupText);
        unitsNeededText = findViewById(R.id.unitsNeededText);
        emergencyDetailsText = findViewById(R.id.emergencyDetailsText);
        statusText = findViewById(R.id.statusText);
        acceptRequestBtn = findViewById(R.id.acceptRequestBtn);
        rejectRequestBtn = findViewById(R.id.rejectRequestBtn);

        // Get request ID from intent
        requestId = getIntent().getStringExtra("requestId");
        if (requestId == null) {
            Toast.makeText(this, "Error: Request details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase references
        requestRef = FirebaseDatabase.getInstance().getReference("emergency_requests").child(requestId);
        responseRef = FirebaseDatabase.getInstance().getReference("emergency_responses");

        loadRequestDetails();
        setupResponseButtons();
    }

    private void loadRequestDetails() {
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String hospitalName = snapshot.child("hospitalName").getValue(String.class);
                    String bloodGroup = snapshot.child("bloodGroup").getValue(String.class);
                    String unitsNeeded = snapshot.child("unitsNeeded").getValue(String.class);
                    String emergencyDetails = snapshot.child("emergencyDetails").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    hospitalNameText.setText("Hospital: " + hospitalName);
                    bloodGroupText.setText("Blood Group: " + bloodGroup);
                    unitsNeededText.setText("Units Needed: " + unitsNeeded);
                    emergencyDetailsText.setText("Details: " + emergencyDetails);
                    statusText.setText("Status: " + status);

                    // Hide response buttons if request is not active
                    boolean isActive = "ACTIVE".equals(status);
                    acceptRequestBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
                    rejectRequestBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestDetailsActivity.this,
                        "Error loading request details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupResponseButtons() {
        acceptRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respondToRequest("ACCEPTED");
            }
        });

        rejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respondToRequest("REJECTED");
            }
        });
    }

    private void respondToRequest(String response) {
        String donorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String responseId = responseRef.push().getKey();

        HashMap<String, Object> responseData = new HashMap<>();
        responseData.put("requestId", requestId);
        responseData.put("donorId", donorId);
        responseData.put("response", response);
        responseData.put("timestamp", ServerValue.TIMESTAMP);

        responseRef.child(responseId).setValue(responseData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update request status if accepted
                            if ("ACCEPTED".equals(response)) {
                                updateRequestStatus(donorId);
                            }
                            Toast.makeText(EmergencyRequestDetailsActivity.this,
                                    "Response submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EmergencyRequestDetailsActivity.this,
                                    "Failed to submit response", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateRequestStatus(String donorId) {
        HashMap<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "IN_PROGRESS");
        statusUpdate.put("acceptedDonorId", donorId);

        requestRef.updateChildren(statusUpdate);
    }
}