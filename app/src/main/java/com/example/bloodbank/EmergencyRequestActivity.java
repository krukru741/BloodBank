package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EmergencyRequestActivity extends AppCompatActivity {

    private EditText hospitalNameEditText, hospitalContactEditText, unitsNeededEditText,
            emergencyDetailsEditText, emergencyContactNameEditText,
            emergencyContactPhoneEditText;
    private Spinner bloodGroupSpinner;
    private RadioGroup priorityRadioGroup;
    private Button submitEmergencyRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request);

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this);

        // Subscribe to emergency notifications topic
        FirebaseMessaging.getInstance().subscribeToTopic("emergency_requests");

        // Initialize views
        hospitalNameEditText = findViewById(R.id.hospitalNameEditText);
        hospitalContactEditText = findViewById(R.id.hospitalContactEditText);
        unitsNeededEditText = findViewById(R.id.unitsNeededEditText);
        emergencyDetailsEditText = findViewById(R.id.emergencyDetailsEditText);
        emergencyContactNameEditText = findViewById(R.id.emergencyContactNameEditText);
        emergencyContactPhoneEditText = findViewById(R.id.emergencyContactPhoneEditText);
        bloodGroupSpinner = findViewById(R.id.emergencyBloodGroupSpinner);
        priorityRadioGroup = findViewById(R.id.priorityRadioGroup);
        submitEmergencyRequest = findViewById(R.id.submitEmergencyRequest);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.SEND_SMS },
                    SMS_PERMISSION_REQUEST_CODE);
        }

        submitEmergencyRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEmergencyRequest();
            }
        });
    }

    private void submitEmergencyRequest() {
        String hospitalName = hospitalNameEditText.getText().toString().trim();
        String hospitalContact = hospitalContactEditText.getText().toString().trim();
        String unitsNeeded = unitsNeededEditText.getText().toString().trim();
        String emergencyDetails = emergencyDetailsEditText.getText().toString().trim();
        String emergencyContactName = emergencyContactNameEditText.getText().toString().trim();
        String emergencyContactPhone = emergencyContactPhoneEditText.getText().toString().trim();
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();

        // Get priority level
        final int priorityLevel;
        final String priorityDescription;
        int selectedPriorityId = priorityRadioGroup.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.priorityUrgent) {
            priorityLevel = 2;
            priorityDescription = "Urgent";
        } else if (selectedPriorityId == R.id.priorityCritical) {
            priorityLevel = 3;
            priorityDescription = "Critical";
        } else {
            priorityLevel = 1;
            priorityDescription = "Normal";
        }

        // Validate inputs
        if (hospitalName.isEmpty() || hospitalContact.isEmpty() || unitsNeeded.isEmpty() ||
                emergencyDetails.isEmpty() || emergencyContactName.isEmpty() ||
                emergencyContactPhone.isEmpty() || bloodGroup.equals("Select Blood Group")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                            createEmergencyRequest(hospitalName, hospitalContact, unitsNeeded,
                                    emergencyDetails, bloodGroup, location,
                                    emergencyContactName, emergencyContactPhone,
                                    priorityLevel, priorityDescription);
                        } else {
                            createEmergencyRequest(hospitalName, hospitalContact, unitsNeeded,
                                    emergencyDetails, bloodGroup, null,
                                    emergencyContactName, emergencyContactPhone,
                                    priorityLevel, priorityDescription);
                        }
                    }
                });
    }

    private void createEmergencyRequest(String hospitalName, String hospitalContact,
            String unitsNeeded, String emergencyDetails,
            String bloodGroup, Location location,
            String emergencyContactName, String emergencyContactPhone,
            int priorityLevel, String priorityDescription) {
        DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference()
                .child("emergency_requests");
        String requestId = emergencyRef.push().getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());

        HashMap<String, Object> emergencyInfo = new HashMap<>();
        emergencyInfo.put("requestId", requestId);
        emergencyInfo.put("userId", userId);
        emergencyInfo.put("hospitalName", hospitalName);
        emergencyInfo.put("hospitalContactNumber", hospitalContact);
        emergencyInfo.put("unitsNeeded", unitsNeeded);
        emergencyInfo.put("emergencyDetails", emergencyDetails);
        emergencyInfo.put("bloodGroup", bloodGroup);
        emergencyInfo.put("status", "ACTIVE");
        emergencyInfo.put("timestamp", currentDate);
        emergencyInfo.put("emergencyContactName", emergencyContactName);
        emergencyInfo.put("emergencyContactPhone", emergencyContactPhone);
        emergencyInfo.put("priorityLevel", priorityLevel);
        emergencyInfo.put("priorityDescription", priorityDescription);

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
                            sendEmergencySMS(emergencyContactPhone, hospitalName, bloodGroup);

                            Toast.makeText(EmergencyRequestActivity.this,
                                    "Emergency request created successfully", Toast.LENGTH_SHORT).show();
                            notifyNearbyDonors(requestId, bloodGroup, location, priorityLevel);
                            finish();
                        } else {
                            Toast.makeText(EmergencyRequestActivity.this,
                                    "Failed to create emergency request", Toast.LENGTH_SHORT).show();
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

    private void notifyNearbyDonors(String requestId, String bloodGroup, Location location,
            int priorityLevel) {
        if (location == null) {
            notifyAllDonors(requestId, bloodGroup, priorityLevel);
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersRef.orderByChild("type").equalTo("donor");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                    User donor = donorSnapshot.getValue(User.class);
                    if (donor != null && isCompatibleBloodGroup(bloodGroup, donor.getBloodgroup())) {
                        if (donorSnapshot.hasChild("latitude") &&
                                donorSnapshot.hasChild("longitude")) {
                            double donorLat = Double.parseDouble(
                                    donorSnapshot.child("latitude").getValue().toString());
                            double donorLon = Double.parseDouble(
                                    donorSnapshot.child("longitude").getValue().toString());

                            if (LocationHelper.isWithinRange(location.getLatitude(),
                                    location.getLongitude(), donorLat, donorLon)) {
                                sendNotificationToDonor(donor, requestId, bloodGroup, priorityLevel);
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

    private void notifyAllDonors(String requestId, String bloodGroup, int priorityLevel) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersRef.orderByChild("type").equalTo("donor");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                    User donor = donorSnapshot.getValue(User.class);
                    if (donor != null && isCompatibleBloodGroup(bloodGroup, donor.getBloodgroup())) {
                        sendNotificationToDonor(donor, requestId, bloodGroup, priorityLevel);
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

    private void sendNotificationToDonor(User donor, String requestId, String bloodGroup,
            int priorityLevel) {
        String priorityTag = priorityLevel == 3 ? "CRITICAL: " : priorityLevel == 2 ? "URGENT: " : "";
        String title = priorityTag + "Emergency Blood Request";
        String message = "Urgent need for " + bloodGroup + " blood. Can you help?";

        // Send notification using NotificationHelper with priority
        NotificationHelper.sendEmergencyNotification(this, title, message, requestId, priorityLevel);

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
        notificationData.put("priorityLevel", priorityLevel);

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
}