package com.example.bloodbank;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView typeText, nameText, genderText, idNumberText, phoneText, addressText, bloodGroupText, birthdateText, emailText;
    private Button backButton;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Load user data
        loadUserData();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        genderText = findViewById(R.id.genderText);
        idNumberText = findViewById(R.id.idNumberText);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        birthdateText = findViewById(R.id.birthdateText);
        emailText = findViewById(R.id.emailText);
        bloodGroupText = findViewById(R.id.bloodgroupText);
        typeText = findViewById(R.id.typeText);
        
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Profile");
    }
    
    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        nameText.setText(user.getName());
                        emailText.setText("Email: " + user.getEmail());
                        phoneText.setText("Phone: " + user.getPhoneNumber());
                        bloodGroupText.setText("Blood Group: " + user.getBloodGroup());
                        typeText.setText("Type: " + user.getType());
                        
                        // Load profile image
                        if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                            Glide.with(UserProfileActivity.this)
                                    .load(new java.io.File(user.getProfileImagePath()))
                                    .placeholder(R.drawable.profile_pic)
                                    .error(R.drawable.profile_pic)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.profile_pic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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