package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.bloodbank.Model.EmergencyResponse;

import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmergencyRequestDetailsActivity extends AppCompatActivity {

    private TextView hospitalNameText, hospitalAddressText, contactNumberText,
            patientNameText, bloodGroupText, unitsNeededText, priorityLevelText,
            emergencyDetailsText, statusText, requestDateText, emergencyTitleText;
    private Button acceptRequestBtn, rejectRequestBtn;
    private String requestId;
    private DatabaseReference requestRef, responseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request_details);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        hospitalNameText = findViewById(R.id.hospitalNameText);
        hospitalAddressText = findViewById(R.id.hospitalAddressText);
        contactNumberText = findViewById(R.id.contactNumberText);
        patientNameText = findViewById(R.id.patientNameText);
        bloodGroupText = findViewById(R.id.bloodGroupText);
        unitsNeededText = findViewById(R.id.unitsNeededText);
        priorityLevelText = findViewById(R.id.priorityLevelText);
        emergencyDetailsText = findViewById(R.id.emergencyDetailsText);
        statusText = findViewById(R.id.statusText);
        requestDateText = findViewById(R.id.requestDateText);
        emergencyTitleText = findViewById(R.id.emergencyTitleText);
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
                    // Get all values as Objects first
                    Object hospitalNameObj = snapshot.child("hospitalName").getValue();
                    Object hospitalAddressObj = snapshot.child("hospitalAddress").getValue();
                    Object hospitalContactObj = snapshot.child("hospitalContact").getValue();
                    Object patientNameObj = snapshot.child("patientName").getValue();
                    Object bloodGroupObj = snapshot.child("bloodGroup").getValue();
                    Object unitsNeededObj = snapshot.child("unitsNeeded").getValue();
                    Object priorityLevelObj = snapshot.child("priorityLevel").getValue();
                    Object emergencyDetailsObj = snapshot.child("emergencyDetails").getValue();
                    Object statusObj = snapshot.child("status").getValue();
                    Object timestampObj = snapshot.child("timestamp").getValue();

                    // Convert to appropriate types
                    String hospitalName = hospitalNameObj != null ? hospitalNameObj.toString() : "Not specified";
                    String hospitalAddress = hospitalAddressObj != null ? hospitalAddressObj.toString() : "Not specified";
                    String hospitalContact = hospitalContactObj != null ? hospitalContactObj.toString() : "Not specified";
                    String patientName = patientNameObj != null ? patientNameObj.toString() : "Not specified";
                    String bloodGroup = bloodGroupObj != null ? bloodGroupObj.toString() : "Not specified";
                    String unitsNeeded = unitsNeededObj != null ? unitsNeededObj.toString() : "Not specified";
                    String priorityLevel = priorityLevelObj != null ? priorityLevelObj.toString() : "Not specified";
                    String emergencyDetails = emergencyDetailsObj != null ? emergencyDetailsObj.toString() : "No additional details provided";
                    String status = statusObj != null ? statusObj.toString() : "Unknown";

                    // Format the date
                    String formattedDate = "Unknown";
                    if (timestampObj != null) {
                        long timestamp = timestampObj instanceof Long ? (Long) timestampObj : 
                                      timestampObj instanceof Double ? ((Double) timestampObj).longValue() : 0;
                        if (timestamp > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            formattedDate = sdf.format(new Date(timestamp));
                        }
                    }

                    // Update UI with all details
                    hospitalNameText.setText("Hospital: " + hospitalName);
                    hospitalAddressText.setText("Address: " + hospitalAddress);
                    contactNumberText.setText("Contact: " + hospitalContact);
                    patientNameText.setText("Patient: " + patientName);
                    bloodGroupText.setText("Blood Group: " + bloodGroup);
                    unitsNeededText.setText("Units Needed: " + unitsNeeded);
                    priorityLevelText.setText("Priority: " + priorityLevel);
                    emergencyDetailsText.setText("Details: " + emergencyDetails);
                    statusText.setText("Status: " + status);
                    requestDateText.setText("Requested: " + formattedDate);

                    // Hide response buttons if request is not active
                    boolean isActive = "ACTIVE".equals(status);
                    acceptRequestBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
                    rejectRequestBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestDetailsActivity.this,
                        "Failed to load request details: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
        responseData.put("status", response);
        responseData.put("timestamp", ServerValue.TIMESTAMP);
        responseData.put("message", response.equals("ACCEPTED") ? "I can help with this request" : "I cannot help with this request");

        responseRef.child(responseId).setValue(responseData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update request status based on response
                            updateRequestStatus(donorId, response);
                            
                            // Add response to the request's responses node
                            addResponseToRequest(responseId, donorId, response);
                            
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

    private void addResponseToRequest(String responseId, String donorId, String status) {
        EmergencyResponse response = new EmergencyResponse(
                donorId,
                status,
                System.currentTimeMillis(),
                status.equals("ACCEPTED") ? "I can help with this request" : "I cannot help with this request"
        );
        
        requestRef.child("responses").child(responseId).setValue(response);
    }

    private void updateRequestStatus(String donorId, String response) {
        HashMap<String, Object> statusUpdate = new HashMap<>();
        
        if ("ACCEPTED".equals(response)) {
        statusUpdate.put("status", "IN_PROGRESS");
        statusUpdate.put("acceptedDonorId", donorId);
        } else if ("REJECTED".equals(response)) {
            // Keep the status as ACTIVE for rejections, but we could add a counter for rejections
            // or implement a different status if needed
            statusUpdate.put("rejectedBy", donorId);
        }

        requestRef.updateChildren(statusUpdate);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}