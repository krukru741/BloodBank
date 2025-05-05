package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImage;
    private MaterialButton changeImageButton, updateButton;
    private TextInputLayout nameLayout, phoneLayout, addressLayout, birthdateLayout;
    private TextInputEditText nameInput, phoneInput, addressInput, birthdateInput;

    private Uri resultUri;
    private ProgressDialog loader;
    private DatabaseReference userRef;
    private File currentPhotoFile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final String APP_DIRECTORY = "BloodBank";
    private static final String IMAGE_DIRECTORY = "ProfileImages";

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        initializeViews();
        
        // Initialize Firebase references
        userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Load current user data
        loadCurrentData();
        
        // Set up date picker
        setupDatePicker();
        
        // Set up image picker
        setupImagePicker();
        
        // Set up update button
        setupUpdateButton();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        changeImageButton = findViewById(R.id.changeImageButton);
        updateButton = findViewById(R.id.updateButton);
        
        nameLayout = findViewById(R.id.nameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        addressLayout = findViewById(R.id.addressLayout);
        birthdateLayout = findViewById(R.id.birthdateLayout);
        
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        birthdateInput = findViewById(R.id.birthdateInput);

        loader = new ProgressDialog(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void loadCurrentData() {
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phonenumber").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String birthdate = snapshot.child("birthdate").getValue(String.class);
                    String profileImagePath = snapshot.child("profileImagePath").getValue(String.class);

                    nameInput.setText(name);
                    phoneInput.setText(phone);
                    addressInput.setText(address);
                    birthdateInput.setText(birthdate);

                    if (profileImagePath != null && !profileImagePath.isEmpty()) {
                        try {
                            File imgFile = new File(profileImagePath);
                            if (imgFile.exists()) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                profileImage.setImageBitmap(myBitmap);
                            }
                        } catch (Exception e) {
                            Toast.makeText(UpdateProfileActivity.this, "Error loading profile image", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Error loading profile: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker() {
        birthdateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UpdateProfileActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        birthdateInput.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupImagePicker() {
        changeImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    private File createImageFile() throws IOException {
        // Create app directory if it doesn't exist
        File appDir = new File(getFilesDir(), APP_DIRECTORY);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        // Create images directory if it doesn't exist
        File imageDir = new File(appDir, IMAGE_DIRECTORY);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Create image file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String imageFileName = "PROFILE_" + timeStamp + ".jpg";
        return new File(imageDir, imageFileName);
    }

    private void setupUpdateButton() {
        updateButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String birthdate = birthdateInput.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError("Name is required");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                phoneLayout.setError("Phone number is required");
                return;
            }
            if (TextUtils.isEmpty(address)) {
                addressLayout.setError("Address is required");
                return;
            }
            if (TextUtils.isEmpty(birthdate)) {
                birthdateLayout.setError("Birthdate is required");
                return;
            }

            loader.setMessage("Updating profile...");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("phonenumber", phone);
            userMap.put("address", address);
            userMap.put("birthdate", birthdate);

            if (currentPhotoFile != null) {
                userMap.put("profileImagePath", currentPhotoFile.getAbsolutePath());
            }

            userRef.updateChildren(userMap).addOnCompleteListener(task -> {
                loader.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Error updating profile: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                resultUri = data.getData();
                try {
                    // Create a new file to save the image
                    currentPhotoFile = createImageFile();
                    
                    // Copy the selected image to the new file
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    FileOutputStream out = new FileOutputStream(currentPhotoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();
                    
                    // Display the image
                    profileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
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