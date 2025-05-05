package com.example.bloodbank;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bloodbank.Adapter.EmergencyRequestAdapter;
import com.example.bloodbank.Model.EmergencyRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmergencyRequestListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<EmergencyRequest> requestList;
    private EmergencyRequestAdapter adapter;

    private DatabaseReference requestsRef;
    private DatabaseReference userRef;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request_list);

        // Initialize Firebase
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference().child("emergency_requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        // Get user type and load appropriate requests
        getUserTypeAndLoadRequests();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        requestList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Emergency Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyRequestAdapter(this, requestList);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadEmergencyRequests();
            }
        });
    }

    private void getUserTypeAndLoadRequests() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userType = snapshot.child("type").getValue().toString();
                    loadEmergencyRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestListActivity.this, "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEmergencyRequests() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Query to get all emergency requests, ordered by priority and timestamp
        Query query = requestsRef.orderByChild("priorityLevel");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    EmergencyRequest request = requestSnapshot.getValue(EmergencyRequest.class);
                    if (request != null) {
                        // For donors: show all requests except their own
                        // For recipients: show only their own requests
                        if ("donor".equals(userType)) {
                            if (!request.getUserId().equals(currentUserId)) {
                                requestList.add(request);
                            }
                        } else if ("recipient".equals(userType)) {
                            if (request.getUserId().equals(currentUserId)) {
                                requestList.add(request);
                            }
                        }
                    }
                }

                // Sort by priority (high to low) and timestamp (recent first)
                Collections.sort(requestList, (r1, r2) -> {
                    // Convert priority strings to numeric values for comparison
                    int p1 = getPriorityValue(r1.getPriorityLevel());
                    int p2 = getPriorityValue(r2.getPriorityLevel());
                    int priorityCompare = Integer.compare(p2, p1);
                    if (priorityCompare != 0) return priorityCompare;
                    return Long.compare(r2.getTimestamp(), r1.getTimestamp());
                });

                adapter.updateList(requestList);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestListActivity.this, "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 0;
        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return 3;
            case "URGENT":
                return 2;
            case "NORMAL":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 