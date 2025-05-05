package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText forgotPass;
    private com.google.android.material.button.MaterialButton loginButton;
    private ProgressDialog loader;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        forgotPass = findViewById(R.id.forgotPass);
        loginButton = findViewById(R.id.loginButton);
        loader = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = forgotPass.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    forgotPass.setError("Email is required!");
                    forgotPass.requestFocus();
                    return;
                }

                loader.setMessage("Sending password reset email...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loader.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, 
                                    "Password reset email sent. Please check your inbox.", 
                                    Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, 
                                    "Error: " + task.getException().getMessage(), 
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loader.dismiss();
                            Toast.makeText(ForgotPasswordActivity.this, 
                                "Error: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    });
            }
        });
    }

    public void onBackToLoginClick(View view) {
        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        finish();
    }
}