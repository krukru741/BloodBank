package com.example.bloodbank;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bloodbank.Model.DonorHealth;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DonorHealthActivity extends AppCompatActivity {
    private TextView lastDonationDateText;
    private TextView totalDonationsText;
    private TextView eligibilityStatusText;
    private TextView daysUntilEligibleText;
    private TextView lastUpdatedText;
    private TextView healthStatusText;
    private TextView deferralReasonText;

    private TextInputEditText hemoglobinInput;
    private TextInputEditText systolicInput;
    private TextInputEditText diastolicInput;
    private TextInputEditText weightInput;
    private TextInputEditText temperatureInput;
    private TextInputEditText pulseRateInput;

    private MaterialCheckBox feelingWellCheckbox;
    private MaterialCheckBox medicationCheckbox;
    private MaterialCheckBox travelCheckbox;
    private MaterialCheckBox surgeryCheckbox;
    private MaterialCheckBox pregnancyCheckbox;

    private MaterialButton updateHealthMetricsButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DonorHealth donorHealth;

    private static final double MIN_HEMOGLOBIN = 12.5; // g/dL
    private static final double MIN_WEIGHT = 50.0; // kg
    private static final int MIN_SYSTOLIC = 90;
    private static final int MAX_SYSTOLIC = 180;
    private static final int MIN_DIASTOLIC = 60;
    private static final int MAX_DIASTOLIC = 100;
    private static final double MIN_TEMPERATURE = 36.0; // °C
    private static final double MAX_TEMPERATURE = 37.5; // °C
    private static final int MIN_PULSE_RATE = 50; // bpm
    private static final int MAX_PULSE_RATE = 100; // bpm
    private static final long DONATION_INTERVAL = TimeUnit.DAYS.toMillis(56); // 56 days between donations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_health);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        initializeViews();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Health Tracking");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load donor health data
        loadDonorHealthData();

        // Set up button click listener
        updateHealthMetricsButton.setOnClickListener(v -> updateHealthMetrics());
    }

    private void initializeViews() {
        lastDonationDateText = findViewById(R.id.lastDonationDateText);
        totalDonationsText = findViewById(R.id.totalDonationsText);
        eligibilityStatusText = findViewById(R.id.eligibilityStatusText);
        daysUntilEligibleText = findViewById(R.id.daysUntilEligibleText);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        healthStatusText = findViewById(R.id.healthStatusText);
        deferralReasonText = findViewById(R.id.deferralReasonText);

        hemoglobinInput = findViewById(R.id.hemoglobinInput);
        systolicInput = findViewById(R.id.systolicInput);
        diastolicInput = findViewById(R.id.diastolicInput);
        weightInput = findViewById(R.id.weightInput);
        temperatureInput = findViewById(R.id.temperatureInput);
        pulseRateInput = findViewById(R.id.pulseRateInput);

        feelingWellCheckbox = findViewById(R.id.feelingWellCheckbox);
        medicationCheckbox = findViewById(R.id.medicationCheckbox);
        travelCheckbox = findViewById(R.id.travelCheckbox);
        surgeryCheckbox = findViewById(R.id.surgeryCheckbox);
        pregnancyCheckbox = findViewById(R.id.pregnancyCheckbox);

        updateHealthMetricsButton = findViewById(R.id.updateHealthMetricsButton);
    }

    private void loadDonorHealthData() {
        if (currentUser == null)
            return;

        DocumentReference donorRef = db.collection("donor_health").document(currentUser.getUid());
        donorRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                donorHealth = documentSnapshot.toObject(DonorHealth.class);
                updateUI();
            } else {
                // Create new donor health record
                donorHealth = new DonorHealth();
                donorHealth.setDonorId(currentUser.getUid());
                donorRef.set(donorHealth);
                updateUI();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load health data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Update donation status
        if (donorHealth.getLastDonationDate() > 0) {
            lastDonationDateText.setText(dateFormat.format(new Date(donorHealth.getLastDonationDate())));
        }

        totalDonationsText.setText(String.valueOf(donorHealth.getTotalDonations()));

        // Update eligibility status based on both donation interval and health status
        long daysUntilEligible = calculateDaysUntilEligible();
        boolean isHealthEligible = "Eligible".equals(donorHealth.getLastHealthStatus());
        
        if (daysUntilEligible > 0 || !isHealthEligible) {
            eligibilityStatusText.setText("Not Eligible");
            daysUntilEligibleText.setText(String.valueOf(daysUntilEligible));
            daysUntilEligibleText.setVisibility(View.VISIBLE);
        } else {
            eligibilityStatusText.setText("Eligible");
            daysUntilEligibleText.setVisibility(View.GONE);
        }

        // Update health status
        if (donorHealth.getLastUpdated() > 0) {
            lastUpdatedText.setText("Last Updated: " +
                    dateFormat.format(new Date(donorHealth.getLastUpdated())));
        }

        healthStatusText.setText("Current Status: " + donorHealth.getLastHealthStatus());

        if (donorHealth.getDeferralReason() != null && !donorHealth.getDeferralReason().isEmpty()) {
            deferralReasonText.setText("Deferral Reason: " + donorHealth.getDeferralReason());
            deferralReasonText.setVisibility(View.VISIBLE);
        } else {
            deferralReasonText.setVisibility(View.GONE);
        }

        // Pre-fill current values in input fields
        if (donorHealth.getHemoglobinLevel() > 0) {
            hemoglobinInput.setText(String.valueOf(donorHealth.getHemoglobinLevel()));
        }
        if (donorHealth.getBloodPressureSystolic() > 0) {
            systolicInput.setText(String.valueOf(donorHealth.getBloodPressureSystolic()));
        }
        if (donorHealth.getBloodPressureDiastolic() > 0) {
            diastolicInput.setText(String.valueOf(donorHealth.getBloodPressureDiastolic()));
        }
        if (donorHealth.getWeight() > 0) {
            weightInput.setText(String.valueOf(donorHealth.getWeight()));
        }
        if (donorHealth.getTemperature() > 0) {
            temperatureInput.setText(String.valueOf(donorHealth.getTemperature()));
        }
        if (donorHealth.getPulseRate() > 0) {
            pulseRateInput.setText(String.valueOf(donorHealth.getPulseRate()));
        }

        // Set checkbox states
        feelingWellCheckbox.setChecked(donorHealth.isFeelingWell());
        medicationCheckbox.setChecked(donorHealth.hasTakenMedication());
        travelCheckbox.setChecked(donorHealth.hasTraveled());
        surgeryCheckbox.setChecked(donorHealth.hasHadSurgery());
        pregnancyCheckbox.setChecked(donorHealth.isPregnant());
    }

    private long calculateDaysUntilEligible() {
        if (donorHealth.getLastDonationDate() == 0)
            return 0;

        long nextEligibleDate = donorHealth.getLastDonationDate() + DONATION_INTERVAL;
        long currentTime = System.currentTimeMillis();
        long daysUntil = TimeUnit.MILLISECONDS.toDays(nextEligibleDate - currentTime);

        return Math.max(0, daysUntil);
    }

    private void updateHealthMetrics() {
        if (currentUser == null)
            return;

        // Validate inputs
        String hemoglobinStr = hemoglobinInput.getText().toString();
        String systolicStr = systolicInput.getText().toString();
        String diastolicStr = diastolicInput.getText().toString();
        String weightStr = weightInput.getText().toString();
        String temperatureStr = temperatureInput.getText().toString();
        String pulseRateStr = pulseRateInput.getText().toString();

        if (hemoglobinStr.isEmpty() || systolicStr.isEmpty() ||
                diastolicStr.isEmpty() || weightStr.isEmpty() ||
                temperatureStr.isEmpty() || pulseRateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double hemoglobin = Double.parseDouble(hemoglobinStr);
            int systolic = Integer.parseInt(systolicStr);
            int diastolic = Integer.parseInt(diastolicStr);
            double weight = Double.parseDouble(weightStr);
            double temperature = Double.parseDouble(temperatureStr);
            int pulseRate = Integer.parseInt(pulseRateStr);

            // Validate health metrics
            StringBuilder deferralReason = new StringBuilder();
            boolean isEligible = true;

            if (hemoglobin < MIN_HEMOGLOBIN) {
                deferralReason.append("Low hemoglobin level. ");
                isEligible = false;
            }

            if (weight < MIN_WEIGHT) {
                deferralReason.append("Below minimum weight requirement. ");
                isEligible = false;
            }

            if (systolic < MIN_SYSTOLIC || systolic > MAX_SYSTOLIC) {
                deferralReason.append("Systolic blood pressure out of range. ");
                isEligible = false;
            }

            if (diastolic < MIN_DIASTOLIC || diastolic > MAX_DIASTOLIC) {
                deferralReason.append("Diastolic blood pressure out of range. ");
                isEligible = false;
            }

            if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
                deferralReason.append("Temperature out of normal range. ");
                isEligible = false;
            }

            if (pulseRate < MIN_PULSE_RATE || pulseRate > MAX_PULSE_RATE) {
                deferralReason.append("Pulse rate out of normal range. ");
                isEligible = false;
            }

            // Check health questions
            if (!feelingWellCheckbox.isChecked()) {
                deferralReason.append("Not feeling well. ");
                isEligible = false;
            }

            if (medicationCheckbox.isChecked()) {
                deferralReason.append("Medication taken in last 24 hours. ");
                isEligible = false;
            }

            if (travelCheckbox.isChecked()) {
                deferralReason.append("Recent international travel. ");
                isEligible = false;
            }

            if (surgeryCheckbox.isChecked()) {
                deferralReason.append("Recent surgery. ");
                isEligible = false;
            }

            if (pregnancyCheckbox.isChecked()) {
                deferralReason.append("Pregnancy or recent pregnancy. ");
                isEligible = false;
            }

            // Update donor health object
            donorHealth.setHemoglobinLevel(hemoglobin);
            donorHealth.setBloodPressureSystolic(systolic);
            donorHealth.setBloodPressureDiastolic(diastolic);
            donorHealth.setWeight(weight);
            donorHealth.setTemperature(temperature);
            donorHealth.setPulseRate(pulseRate);
            donorHealth.setFeelingWell(feelingWellCheckbox.isChecked());
            donorHealth.setTakenMedication(medicationCheckbox.isChecked());
            donorHealth.setTraveled(travelCheckbox.isChecked());
            donorHealth.setHadSurgery(surgeryCheckbox.isChecked());
            donorHealth.setPregnant(pregnancyCheckbox.isChecked());
            donorHealth.setLastUpdated(System.currentTimeMillis());
            donorHealth.setLastHealthStatus(isEligible ? "Eligible" : "Deferred");
            donorHealth.setDeferralReason(isEligible ? "" : deferralReason.toString().trim());

            // Save to Firebase
            db.collection("donor_health").document(currentUser.getUid())
                    .set(donorHealth)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Health metrics updated successfully",
                                Toast.LENGTH_SHORT).show();
                        updateUI();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update health metrics: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}