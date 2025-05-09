package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.bloodbank.Model.User;
import com.example.bloodbank.Util.LocationHelper;
import com.example.bloodbank.Util.NotificationHelper;
import com.google.firebase.database.ServerValue;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EmergencyRequestActivity extends AppCompatActivity {

    private TextInputEditText hospitalNameInput, hospitalAddressInput, hospitalContactInput,
            patientNameInput, unitsNeededInput, emergencyDetailsInput;
    private AutoCompleteTextView bloodGroupInput, priorityLevelInput;
    private DatabaseReference emergencyRequestsRef;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private static final String[] BLOOD_GROUPS = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };
    private static final String[] PRIORITY_LEVELS = {
        "Normal", "Urgent", "Critical"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this);

        // Subscribe to emergency notifications topic
        FirebaseMessaging.getInstance().subscribeToTopic("emergency_requests");

        // Initialize Firebase
        emergencyRequestsRef = FirebaseDatabase.getInstance().getReference("emergency_requests");

        // Initialize views
        hospitalNameInput = findViewById(R.id.hospitalNameInput);
        hospitalAddressInput = findViewById(R.id.hospitalAddressInput);
        hospitalContactInput = findViewById(R.id.hospitalContactInput);
        patientNameInput = findViewById(R.id.patientNameInput);
        bloodGroupInput = findViewById(R.id.bloodGroupInput);
        priorityLevelInput = findViewById(R.id.priorityLevelInput);
        unitsNeededInput = findViewById(R.id.unitsNeededInput);
        emergencyDetailsInput = findViewById(R.id.emergencyDetailsInput);

        // Setup blood group spinner
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            BLOOD_GROUPS
        );
        bloodGroupInput.setAdapter(bloodGroupAdapter);
        bloodGroupInput.setKeyListener(null); // Disable keyboard input
        bloodGroupInput.setOnClickListener(v -> bloodGroupInput.showDropDown());
        bloodGroupInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBloodGroup = BLOOD_GROUPS[position];
            bloodGroupInput.setText(selectedBloodGroup, false);
        });

        // Setup priority level spinner
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            PRIORITY_LEVELS
        );
        priorityLevelInput.setAdapter(priorityAdapter);
        priorityLevelInput.setKeyListener(null); // Disable keyboard input
        priorityLevelInput.setOnClickListener(v -> priorityLevelInput.showDropDown());
        priorityLevelInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPriority = PRIORITY_LEVELS[position];
            priorityLevelInput.setText(selectedPriority, false);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.SEND_SMS },
                    SMS_PERMISSION_REQUEST_CODE);
        }

        // Set up submit button
        findViewById(R.id.submitRequestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEmergencyRequest();
            }
        });
    }

    private void submitEmergencyRequest() {
        // Get input values
        String hospitalName = hospitalNameInput.getText().toString().trim();
        String hospitalAddress = hospitalAddressInput.getText().toString().trim();
        String hospitalContact = hospitalContactInput.getText().toString().trim();
        String patientName = patientNameInput.getText().toString().trim();
        String bloodGroup = bloodGroupInput.getText().toString().trim();
        String priorityLevel = priorityLevelInput.getText().toString().trim();
        String unitsNeeded = unitsNeededInput.getText().toString().trim();
        String emergencyDetails = emergencyDetailsInput.getText().toString().trim();

        // Validate inputs
        if (hospitalName.isEmpty() || hospitalAddress.isEmpty() || hospitalContact.isEmpty() ||
                patientName.isEmpty() || bloodGroup.isEmpty() || priorityLevel.isEmpty() ||
                unitsNeeded.isEmpty() || emergencyDetails.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get current location and create emergency request
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            createEmergencyRequest(hospitalName, hospitalAddress, hospitalContact,
                                    patientName, bloodGroup, unitsNeeded, emergencyDetails, location);
                        } else {
                            createEmergencyRequest(hospitalName, hospitalAddress, hospitalContact,
                                    patientName, bloodGroup, unitsNeeded, emergencyDetails, null);
                        }
                    }
                });
    }

    private void createEmergencyRequest(String hospitalName, String hospitalAddress, String hospitalContact,
            String patientName, String bloodGroup, String unitsNeeded, String emergencyDetails,
            Location location) {
        DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference()
                .child("emergency_requests");
        String requestId = emergencyRef.push().getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long currentTimestamp = System.currentTimeMillis();

        HashMap<String, Object> emergencyInfo = new HashMap<>();
        emergencyInfo.put("requestId", requestId);
        emergencyInfo.put("userId", userId);
        emergencyInfo.put("hospitalName", hospitalName);
        emergencyInfo.put("hospitalAddress", hospitalAddress);
        emergencyInfo.put("hospitalContact", hospitalContact);
        emergencyInfo.put("patientName", patientName);
        emergencyInfo.put("bloodGroup", bloodGroup);
        emergencyInfo.put("priorityLevel", priorityLevelInput.getText().toString());
        emergencyInfo.put("unitsNeeded", unitsNeeded);
        emergencyInfo.put("emergencyDetails", emergencyDetails);
        emergencyInfo.put("status", "ACTIVE");
        emergencyInfo.put("timestamp", currentTimestamp);
        emergencyInfo.put("requestedBy", userId);

        if (location != null) {
            emergencyInfo.put("latitude", location.getLatitude());
            emergencyInfo.put("longitude", location.getLongitude());
        }

        emergencyRef.child(requestId).setValue(emergencyInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Send SMS to emergency contact
                            sendEmergencySMS(hospitalContact, hospitalName, bloodGroup);

                            Toast.makeText(EmergencyRequestActivity.this,
                                    "Emergency request submitted successfully", Toast.LENGTH_SHORT).show();
                            notifyNearbyDonors(requestId, bloodGroup, location);
                            finish();
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error";
                            Toast.makeText(EmergencyRequestActivity.this,
                                    "Failed to submit request: " + errorMessage, 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendEmergencySMS(String phoneNumber, String hospitalName, String bloodGroup) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String message = "EMERGENCY: Blood donation needed at " + hospitalName +
                        " for blood group " + bloodGroup + ". Please respond ASAP.";
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void notifyNearbyDonors(String requestId, String bloodGroup, Location location) {
        if (location == null) {
            notifyAllDonors(requestId, bloodGroup);
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersRef.orderByChild("type").equalTo("donor");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                    User donor = donorSnapshot.getValue(User.class);
                    if (donor != null && isCompatibleBloodGroup(bloodGroup, donor.getBloodGroup())) {
                        if (donorSnapshot.hasChild("latitude") &&
                                donorSnapshot.hasChild("longitude")) {
                            double donorLat = Double.parseDouble(
                                    donorSnapshot.child("latitude").getValue().toString());
                            double donorLon = Double.parseDouble(
                                    donorSnapshot.child("longitude").getValue().toString());

                            if (LocationHelper.isWithinRange(location.getLatitude(),
                                    location.getLongitude(), donorLat, donorLon)) {
                                sendNotificationToDonor(donor, requestId, bloodGroup);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestActivity.this,
                        "Failed to notify donors: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyAllDonors(String requestId, String bloodGroup) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersRef.orderByChild("type").equalTo("donor");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                    User donor = donorSnapshot.getValue(User.class);
                    if (donor != null && isCompatibleBloodGroup(bloodGroup, donor.getBloodGroup())) {
                        sendNotificationToDonor(donor, requestId, bloodGroup);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestActivity.this,
                        "Failed to notify donors: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotificationToDonor(User donor, String requestId, String bloodGroup) {
        String title = "Emergency Blood Request";
        String message = "Urgent need for " + bloodGroup + " blood. Can you help?";

        // Send notification using NotificationHelper with priority level 3 (Critical)
        NotificationHelper.sendEmergencyNotification(this, title, message, requestId, 3);

        // Store notification in Firebase
        DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(donor.getId())
                .push();

        HashMap<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("requestId", requestId);
        notificationData.put("timestamp", ServerValue.TIMESTAMP);
        notificationData.put("read", false);

        notificationRef.setValue(notificationData);
    }

    private boolean isCompatibleBloodGroup(String requestedBloodGroup, String donorBloodGroup) {
        // Blood group compatibility logic
        switch (requestedBloodGroup) {
            case "A+":
                return donorBloodGroup.equals("A+") || donorBloodGroup.equals("A-") ||
                        donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "A-":
                return donorBloodGroup.equals("A-") || donorBloodGroup.equals("O-");
            case "B+":
                return donorBloodGroup.equals("B+") || donorBloodGroup.equals("B-") ||
                        donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "B-":
                return donorBloodGroup.equals("B-") || donorBloodGroup.equals("O-");
            case "AB+":
                return true; // Can receive from all blood groups
            case "AB-":
                return donorBloodGroup.endsWith("-"); // Can receive from all negative blood groups
            case "O+":
                return donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "O-":
                return donorBloodGroup.equals("O-");
            default:
                return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}