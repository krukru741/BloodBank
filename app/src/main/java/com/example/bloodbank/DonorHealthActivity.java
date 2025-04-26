package com.example.bloodbank;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bloodbank.Model.DonorHealth;
import com.google.android.material.button.MaterialButton;
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
            lastDonationDateText.setText("Last Donation: " +
                    dateFormat.format(new Date(donorHealth.getLastDonationDate())));
        }

        totalDonationsText.setText("Total Donations: " + donorHealth.getTotalDonations());

        // Update eligibility status
        long daysUntilEligible = calculateDaysUntilEligible();
        if (daysUntilEligible > 0) {
            eligibilityStatusText.setText("Eligibility Status: Not Eligible");
            daysUntilEligibleText.setText("Days until eligible: " + daysUntilEligible);
            daysUntilEligibleText.setVisibility(View.VISIBLE);
        } else {
            eligibilityStatusText.setText("Eligibility Status: Eligible");
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

        if (hemoglobinStr.isEmpty() || systolicStr.isEmpty() ||
                diastolicStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double hemoglobin = Double.parseDouble(hemoglobinStr);
            int systolic = Integer.parseInt(systolicStr);
            int diastolic = Integer.parseInt(diastolicStr);
            double weight = Double.parseDouble(weightStr);

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

            // Update donor health object
            donorHealth.setHemoglobinLevel(hemoglobin);
            donorHealth.setBloodPressureSystolic(systolic);
            donorHealth.setBloodPressureDiastolic(diastolic);
            donorHealth.setWeight(weight);
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