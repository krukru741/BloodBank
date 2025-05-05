package com.example.bloodbank;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ScheduleDonationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText dateInput, timeInput, centerInput, notesInput;
    private Button scheduleButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat, timeFormat;
    private DatabaseReference donationRef;
    private String userId;
    private FirebaseFirestore db;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_donation);

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        donationRef = FirebaseDatabase.getInstance().getReference().child("donations");
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Initialize date and time formatters
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        
        // Set up date picker
        dateInput.setOnClickListener(v -> showDatePicker());
        dateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });
        
        // Set up time picker
        timeInput.setOnClickListener(v -> showTimePicker());
        timeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showTimePicker();
            }
        });
        
        // Set up donation center picker
        centerInput.setOnClickListener(v -> showDonationCenterPicker());
        centerInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDonationCenterPicker();
            }
        });
        
        // Set up schedule button
        scheduleButton.setOnClickListener(v -> scheduleDonation());
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        centerInput = findViewById(R.id.centerInput);
        notesInput = findViewById(R.id.notesInput);
        scheduleButton = findViewById(R.id.scheduleButton);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Schedule Donation");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateInput.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        // Set maximum date to 3 months from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    timeInput.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }
    
    private void showDonationCenterPicker() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_donation_center_picker, null);
        RecyclerView centersRecyclerView = dialogView.findViewById(R.id.centersRecyclerView);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Setup RecyclerView
        centersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<DonationCenter> centersList = new ArrayList<>();
        DonationCenterPickerAdapter adapter = new DonationCenterPickerAdapter(centersList, center -> {
            centerInput.setText(center.getName());
            dialog.dismiss();
        });
        centersRecyclerView.setAdapter(adapter);

        // Load donation centers
        db.collection("donation_centers")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    centersList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DonationCenter center = doc.toObject(DonationCenter.class);
                        center.setId(doc.getId());
                        centersList.add(center);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading centers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();

        // Setup cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    
    private void scheduleDonation() {
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String center = centerInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();
        
        // Validate inputs
        if (date.isEmpty()) {
            dateInput.setError("Please select a date");
            return;
        }
        
        if (time.isEmpty()) {
            timeInput.setError("Please select a time");
            return;
        }
        
        if (center.isEmpty()) {
            centerInput.setError("Please enter a donation center");
            return;
        }
        
        // Create donation record
        String donationId = UUID.randomUUID().toString();
        HashMap<String, Object> donationMap = new HashMap<>();
        donationMap.put("donationId", donationId);
        donationMap.put("donorId", userId);
        donationMap.put("donationDate", date + " " + time);
        donationMap.put("donationCenter", center);
        donationMap.put("notes", notes);
        donationMap.put("status", "scheduled");
        donationMap.put("timestamp", System.currentTimeMillis());
        
        // Save to Firebase
        donationRef.child(donationId).updateChildren(donationMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ScheduleDonationActivity.this, 
                            "Donation scheduled successfully", Toast.LENGTH_SHORT).show();
                    
                    // Update donor's last donation date
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(userId);
                    userRef.child("lastdonation").setValue(date)
                            .addOnSuccessListener(aVoid1 -> {
                                // Return to main activity
                                Intent intent = new Intent(ScheduleDonationActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ScheduleDonationActivity.this, 
                            "Failed to schedule donation: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 