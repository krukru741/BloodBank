package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bloodbank.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText forgotPass;
    private Button loginButton;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener authStateListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user !=null){
                    Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        };

        forgotPass = findViewById(R.id.forgotPass);
        loginButton = findViewById(R.id.loginButton);

        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = forgotPass.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    forgotPass.setError("Email is required!");
                } else {
                    loader.setMessage("loading....");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.sendPasswordResetEmail(forgotPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                finish();
                                Toast.makeText(ForgotPasswordActivity.this, "Please Check your Email Address", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Enter correct email Address", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        }

                    });
                }
            }
        });
    }
}