package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecipientRegistrationActivity extends AppCompatActivity {

    private TextView backButton;

    private CircleImageView profile_image;
    private TextInputEditText registeredFullName, registeredIdNumber, registeredPhoneNumber,
            registeredAddress, registeredEmail, registeredPassword, registeredDate;
    private MaterialAutoCompleteTextView bloodGroupsSpinner;
    private MaterialAutoCompleteTextView gendersSpinner;
    private Button registerButton;

    private Uri resultUri;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private static final String APP_DIRECTORY = "BloodBank";
    private static final String IMAGE_DIRECTORY = "ProfileImages";
    private File currentPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_registration);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecipientRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        profile_image = findViewById(R.id.profile_image);
        registeredFullName = findViewById(R.id.registeredFullName);
        registeredIdNumber = findViewById(R.id.registeredIdNumber);
        registeredPhoneNumber = findViewById(R.id.registeredPhoneNumber);
        registeredAddress = findViewById(R.id.registeredAddress);
        registeredEmail = findViewById(R.id.registeredEmail);
        registeredPassword = findViewById(R.id.registeredPassword);
        registeredDate = findViewById(R.id.registeredDate);
        gendersSpinner = findViewById(R.id.gendersSpinner);
        bloodGroupsSpinner = findViewById(R.id.bloodGroupsSpinner);
        registerButton = findViewById(R.id.registerButton);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        // Initialize gender spinner
        String[] genderItems = new String[]{"Select your gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderItems);
        gendersSpinner.setAdapter(genderAdapter);

        // Initialize blood group spinner
        String[] bloodGroupItems = new String[]{"Select your blood group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroupItems);
        bloodGroupsSpinner.setAdapter(bloodGroupAdapter);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        // Initialize date picker components
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set up date picker
        registeredDate.setOnClickListener(v -> showDatePicker());
        registeredDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = registeredEmail.getText().toString().trim();
                final String password = registeredPassword.getText().toString().trim();
                final String fullName = registeredFullName.getText().toString().trim();
                final String idNumber = registeredIdNumber.getText().toString().trim();
                final String phoneNumber = registeredPhoneNumber.getText().toString().trim();
                final String address = registeredAddress.getText().toString().trim();
                final String date = registeredDate.getText().toString().trim();
                final String gender = gendersSpinner.getText().toString();
                final String bloodGroup = bloodGroupsSpinner.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    registeredEmail.setError("Email is required!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    registeredPassword.setError("Password is required!");
                    return;
                }
                if (TextUtils.isEmpty(fullName)) {
                    registeredFullName.setError("Full Name is required is required!");
                    return;
                }
                if (TextUtils.isEmpty(idNumber)) {
                    registeredIdNumber.setError("National ID Number is required!");
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    registeredPhoneNumber.setError("Phone Number is required!");
                    return;
                }
                if (TextUtils.isEmpty(address)) {
                    registeredAddress.setError("Address is required");
                }
                if (TextUtils.isEmpty(date)) {
                    registeredDate.setError("Enter your birthdate");
                    return;
                }
                if (!isValidDate(date)) {
                    return;
                }
                if (gender.equals("Select your gender")) {
                    Toast.makeText(RecipientRegistrationActivity.this, "Select Gender", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (bloodGroup.equals("Select your blood group")) {
                    Toast.makeText(RecipientRegistrationActivity.this, "Select Blood group", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    loader.setMessage("Registering you...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){

                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(RecipientRegistrationActivity.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RecipientRegistrationActivity.this, "onFailure: Email not sent", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                String error = task.getException().toString();
                                Toast.makeText(RecipientRegistrationActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                userDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(currentUserId);
                                HashMap userInfo = new HashMap();
                                userInfo.put("id", currentUserId);
                                userInfo.put("name", fullName);
                                userInfo.put("email", email);
                                userInfo.put("idnumber", idNumber);
                                userInfo.put("phoneNumber", phoneNumber);
                                userInfo.put("password", password);
                                userInfo.put("address", address);
                                userInfo.put("birthdate", date);
                                userInfo.put("gender", gender);
                                userInfo.put("bloodGroup", bloodGroup);
                                userInfo.put("type", "recipient");
                                userInfo.put("search", "recipient"+bloodGroup);

                                // If we have a profile image, save its path
                                if (currentPhotoFile != null && currentPhotoFile.exists()) {
                                    userInfo.put("profileImagePath", currentPhotoFile.getAbsolutePath());
                                }

                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RecipientRegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RecipientRegistrationActivity.this, VerifyEmailActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RecipientRegistrationActivity.this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        loader.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);
            
            try {
                // Create directory if it doesn't exist
                File storageDir = new File(getFilesDir(), APP_DIRECTORY + File.separator + IMAGE_DIRECTORY);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }

                // Create a unique filename
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                currentPhotoFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
                );

                // Save the image to internal storage
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                FileOutputStream out = new FileOutputStream(currentPhotoFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(RecipientRegistrationActivity.this, "Error saving profile image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField();
        };

        new DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateField() {
        registeredDate.setText(dateFormat.format(calendar.getTime()));
    }

    private boolean isValidDate(String dateStr) {
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar inputDate = Calendar.getInstance();
            inputDate.setTime(date);
            
            // Get current date
            Calendar currentDate = Calendar.getInstance();
            
            // Check if birth date is in the future
            if (inputDate.after(currentDate)) {
                registeredDate.setError("Birth date cannot be in the future");
                return false;
            }
            
            // Check if age is at least 18 years
            Calendar minAgeDate = Calendar.getInstance();
            minAgeDate.add(Calendar.YEAR, -18);
            if (inputDate.after(minAgeDate)) {
                registeredDate.setError("You must be at least 18 years old");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            registeredDate.setError("Invalid date format");
            return false;
        }
    }
}