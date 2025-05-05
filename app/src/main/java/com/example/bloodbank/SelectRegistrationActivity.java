package com.example.bloodbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class SelectRegistrationActivity extends AppCompatActivity {

    private MaterialButton donorButton, recipientButton;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_registration);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        donorButton = findViewById(R.id.donorButton);
        recipientButton = findViewById(R.id.recipientButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        donorButton.setOnClickListener(v -> {
            navigateToActivity(DonorRegistrationActivity.class);
        });

        recipientButton.setOnClickListener(v -> {
            navigateToActivity(RecipientRegistrationActivity.class);
        });

        backButton.setOnClickListener(v -> {
            navigateToActivity(LoginActivity.class);
        });
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SelectRegistrationActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up references
        donorButton = null;
        recipientButton = null;
        backButton = null;
    }
}