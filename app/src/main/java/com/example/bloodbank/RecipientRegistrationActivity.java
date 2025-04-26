package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecipientRegistrationActivity extends AppCompatActivity {

    private TextView backButton;

    private CircleImageView profile_image;
    private TextInputEditText registeredFullName, registeredIdNumber, registeredPhoneNumber,
            registeredAddress, registeredEmail, registeredPassword, registeredDate;
    private Spinner bloodGroupsSpinner;
    private Spinner gendersSpinner;
    private Button registerButton;

    private Uri resultUri;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

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

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
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
                final String gender = gendersSpinner.getSelectedItem().toString();
                final String bloodGroup = bloodGroupsSpinner.getSelectedItem().toString();

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
                                userInfo.put("phonenumber", phoneNumber);
                                userInfo.put("password", password);
                                userInfo.put("address", address);
                                userInfo.put("birthdate", date);
                                userInfo.put("gender", gender);
                                userInfo.put("bloodgroup", bloodGroup);
                                userInfo.put("type", "recipient");
                                userInfo.put("search", "recipient"+bloodGroup);

                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(RecipientRegistrationActivity.this, "Data set successfully", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(RecipientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        finish();
                                        loader.dismiss();
                                    }
                                });

                                if (resultUri !=null){
                                    final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                                            .child("profile images").child(currentUserId);
                                    Bitmap bitmap = null;

                                    try {
                                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                                    byte[] data  = byteArrayOutputStream.toByteArray();
                                    UploadTask uploadTask = filePath.putBytes(data);

                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RecipientRegistrationActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            if (taskSnapshot.getMetadata() !=null && taskSnapshot.getMetadata().getReference() !=null){
                                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imageUrl = uri.toString();
                                                        Map newImageMap = new HashMap();
                                                        newImageMap.put("profilepictureurl", imageUrl);
                                                        userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()){
//                                                                    Toast.makeText(RecipientRegistrationActivity.this, "Image url added to database successfully", Toast.LENGTH_SHORT).show();
                                                                }else {
                                                                    Toast.makeText(RecipientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                        finish();
                                                    }
                                                });
                                            }

                                        }
                                    });


                                }

                                Intent intent = new Intent(RecipientRegistrationActivity.this, VerifyEmailActivity.class);
                                startActivity(intent);
                                finish();
                                loader.dismiss();
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
        if (requestCode ==1 && resultCode == RESULT_OK && data !=null){
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);
        }
    }
}