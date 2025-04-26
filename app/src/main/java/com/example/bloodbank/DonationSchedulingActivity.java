package com.example.bloodbank;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bloodbank.Model.DonationAppointment;
import com.example.bloodbank.Model.DonationCenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DonationSchedulingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner centerSpinner, timeSlotSpinner;
    private CalendarView calendarView;
    private TextInputEditText notesEditText;
    private MaterialButton scheduleButton;

    private List<DonationCenter> centers;
    private List<String> timeSlots;
    private DonationCenter selectedCenter;
    private String selectedTimeSlot;
    private long selectedDate;

    private DatabaseReference centersRef, appointmentsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_scheduling);

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        centersRef = FirebaseDatabase.getInstance().getReference().child("donation_centers");
        appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");

        // Initialize views
        initializeViews();
        setupToolbar();
        loadDonationCenters();
        setupTimeSlots();
        setupCalendar();
        setupScheduleButton();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        centerSpinner = findViewById(R.id.centerSpinner);
        timeSlotSpinner = findViewById(R.id.timeSlotSpinner);
        calendarView = findViewById(R.id.calendarView);
        notesEditText = findViewById(R.id.notesEditText);
        scheduleButton = findViewById(R.id.scheduleButton);

        centers = new ArrayList<>();
        timeSlots = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Schedule Donation");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadDonationCenters() {
        centersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                centers.clear();
                List<String> centerNames = new ArrayList<>();

                for (DataSnapshot centerSnapshot : snapshot.getChildren()) {
                    DonationCenter center = centerSnapshot.getValue(DonationCenter.class);
                    if (center != null && center.isActive()) {
                        centers.add(center);
                        centerNames.add(center.getName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        DonationSchedulingActivity.this,
                        android.R.layout.simple_spinner_item,
                        centerNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                centerSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DonationSchedulingActivity.this,
                        "Failed to load donation centers", Toast.LENGTH_SHORT).show();
            }
        });

        centerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCenter = centers.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCenter = null;
            }
        });
    }

    private void setupTimeSlots() {
        // Add time slots from 9 AM to 5 PM
        timeSlots.add("9:00 AM");
        timeSlots.add("10:00 AM");
        timeSlots.add("11:00 AM");
        timeSlots.add("12:00 PM");
        timeSlots.add("1:00 PM");
        timeSlots.add("2:00 PM");
        timeSlots.add("3:00 PM");
        timeSlots.add("4:00 PM");
        timeSlots.add("5:00 PM");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);

        timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeSlot = timeSlots.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimeSlot = null;
            }
        });
    }

    private void setupCalendar() {
        // Set minimum date to tomorrow
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        calendarView.setMinDate(minDate.getTimeInMillis());

        // Set maximum date to 3 months from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
        });

        // Set initial selected date to tomorrow
        selectedDate = minDate.getTimeInMillis();
        calendarView.setDate(selectedDate);
    }

    private void setupScheduleButton() {
        scheduleButton.setOnClickListener(v -> {
            if (validateInput()) {
                scheduleAppointment();
            }
        });
    }

    private boolean validateInput() {
        if (selectedCenter == null) {
            Toast.makeText(this, "Please select a donation center", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedDate == 0) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void scheduleAppointment() {
        String appointmentId = appointmentsRef.push().getKey();
        String notes = notesEditText.getText().toString().trim();

        DonationAppointment appointment = new DonationAppointment(
                appointmentId,
                userId,
                selectedCenter.getCenterId(),
                selectedDate,
                selectedTimeSlot,
                "SCHEDULED",
                notes);

        appointmentsRef.child(appointmentId).setValue(appointment)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Appointment scheduled successfully",
                                Toast.LENGTH_SHORT).show();
                        addToCalendar();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to schedule appointment",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", selectedDate);
        intent.putExtra("endTime", selectedDate + 3600000); // 1 hour duration
        intent.putExtra("title", "Blood Donation Appointment");
        intent.putExtra("description", "Blood donation appointment at " +
                selectedCenter.getName());
        intent.putExtra("eventLocation", selectedCenter.getAddress());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}