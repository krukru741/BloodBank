package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView nameText, genderText, idNumberText, emailText, phoneText, addressText, birthdateText, bloodgroupText, typeText;
    private MaterialButton updateButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        initializeViews();
        
        // Initialize Firebase reference
        userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Load user data
        loadUserData();
        
        // Set up update button
        setupUpdateButton();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        genderText = findViewById(R.id.genderText);
        idNumberText = findViewById(R.id.idNumberText);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        birthdateText = findViewById(R.id.birthdateText);
        bloodgroupText = findViewById(R.id.bloodgroupText);
        typeText = findViewById(R.id.typeText);
        updateButton = findViewById(R.id.updateButton);

        // Verify all views are initialized
        if (profileImage == null || nameText == null || emailText == null || 
            genderText == null || idNumberText == null || phoneText == null || 
            addressText == null || birthdateText == null || bloodgroupText == null || 
            typeText == null || updateButton == null) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);
                    String userPhone = snapshot.child("phonenumber").getValue(String.class);
                    String userAddress = snapshot.child("address").getValue(String.class);
                    String userBirthdate = snapshot.child("birthdate").getValue(String.class);
                    String userBloodGroup = snapshot.child("bloodgroup").getValue(String.class);
                    String userType = snapshot.child("type").getValue(String.class);
                    String userGender = snapshot.child("gender").getValue(String.class);
                    String userIdNumber = snapshot.child("idnumber").getValue(String.class);
                    String profileImagePath = snapshot.child("profileImagePath").getValue(String.class);

                    // Set text with labels
                    if (nameText != null && userName != null) nameText.setText(userName);
                    if (emailText != null && userEmail != null) emailText.setText("Email: " + userEmail);
                    if (phoneText != null && userPhone != null) phoneText.setText("Phone: " + userPhone);
                    if (addressText != null && userAddress != null) addressText.setText("Address: " + userAddress);
                    if (birthdateText != null && userBirthdate != null) birthdateText.setText("Birthdate: " + userBirthdate);
                    if (bloodgroupText != null && userBloodGroup != null) bloodgroupText.setText("Blood Group: " + userBloodGroup);
                    if (typeText != null && userType != null) typeText.setText(userType);
                    if (genderText != null && userGender != null) genderText.setText("Gender: " + userGender);
                    if (idNumberText != null && userIdNumber != null) idNumberText.setText("ID Number: " + userIdNumber);

                    if (profileImagePath != null && !profileImagePath.isEmpty() && profileImage != null) {
                        try {
                            File imgFile = new File(profileImagePath);
                            if (imgFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                profileImage.setImageBitmap(bitmap);
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this, "Error loading profile image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUpdateButton() {
        updateButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, UpdateProfileActivity.class));
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