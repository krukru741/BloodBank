package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bloodbank.Adapter.UserAdapter;
import com.example.bloodbank.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SentEmailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recycleView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View progressBar;
    private TextView emptyView;
    private LinearLayout errorContainer;
    private TextView errorView;
    private Button retryButton;

    private List<String> idList;
    private List<User> userList;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;
    private ValueEventListener emailListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_email);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(SentEmailActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Initialize Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        
        // Setup retry button
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEmails();
            }
        });
        
        // Load emails
        loadEmails();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recycleView = findViewById(R.id.recycleView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        errorContainer = findViewById(R.id.errorContainer);
        errorView = findViewById(R.id.errorView);
        retryButton = findViewById(R.id.retryButton);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Donors Emailed");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        userAdapter = new UserAdapter(SentEmailActivity.this, userList);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.setAdapter(userAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadEmails);
    }
    
    private void loadEmails() {
        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(SentEmailActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        
        // Remove previous listener if exists
        if (emailListener != null) {
            DatabaseReference emailsRef = FirebaseDatabase.getInstance().getReference().child("emails")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            emailsRef.removeEventListener(emailListener);
        }
        
        // Get list of users who were emailed
        DatabaseReference emailsRef = FirebaseDatabase.getInstance().getReference().child("emails")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        emailListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                userList.clear();
                
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }

                    // Load user details for each ID
                    loadUserDetails();
                } else {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                // Show error message
                errorContainer.setVisibility(View.VISIBLE);
                
                // Check if it's a permission denied error
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    errorView.setText("Permission denied. Please make sure you're logged in.");
                    
                    // Check if user is still logged in
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        // User is not logged in, redirect to login
                        Intent intent = new Intent(SentEmailActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    errorView.setText("Error loading emails: " + error.getMessage());
                }
            }
        };
        
        emailsRef.addValueEventListener(emailListener);
    }
    
    private void loadUserDetails() {
        if (idList.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                
                for (String userId : idList) {
                    if (snapshot.child(userId).exists()) {
                        User user = snapshot.child(userId).getValue(User.class);
                        if (user != null) {
                            user.setId(userId);
                            userList.add(user);
                        }
                    }
                }

                userAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                if (userList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                // Show error message
                errorContainer.setVisibility(View.VISIBLE);
                errorView.setText("Error loading user details: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener when activity is destroyed
        if (emailListener != null) {
            DatabaseReference emailsRef = FirebaseDatabase.getInstance().getReference().child("emails")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            emailsRef.removeEventListener(emailListener);
        }
    }
}