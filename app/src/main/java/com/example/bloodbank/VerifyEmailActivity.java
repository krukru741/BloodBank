package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private TextView verifyMsg;
    private TextView verifySubtitle;
    private MaterialButton verifyBtn;
    private FirebaseAuth mAuth;
    private MaterialAlertDialogBuilder progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        initializeViews();
        setupFirebase();
        checkEmailVerification();
    }

    private void initializeViews() {
        verifyMsg = findViewById(R.id.verifyMsg);
        verifySubtitle = findViewById(R.id.verifySubtitle);
        verifyBtn = findViewById(R.id.verifyBtn);
        progressDialog = new MaterialAlertDialogBuilder(this)
                .setMessage("Sending verification email...")
                .setCancelable(false);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            showVerificationUI();
            setupVerificationButton();
        } else {
            navigateToMainActivity();
        }
    }

    private void showVerificationUI() {
        verifyMsg.setVisibility(View.VISIBLE);
        verifySubtitle.setVisibility(View.VISIBLE);
        verifyBtn.setVisibility(View.VISIBLE);
    }

    private void setupVerificationButton() {
        verifyBtn.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                sendVerificationEmail(user);
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        progressDialog.show();
        
        user.sendEmailVerification()
            .addOnSuccessListener(unused -> {
                progressDialog.create().dismiss();
                Toast.makeText(VerifyEmailActivity.this, 
                    "Verification email has been sent", 
                    Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            })
            .addOnFailureListener(e -> {
                progressDialog.create().dismiss();
                Toast.makeText(VerifyEmailActivity.this, 
                    "Failed to send verification email: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}