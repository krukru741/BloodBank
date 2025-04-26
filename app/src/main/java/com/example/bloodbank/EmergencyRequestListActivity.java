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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request_list);

        // Initialize Firebase
        requestsRef = FirebaseDatabase.getInstance().getReference().child("emergency_requests");

        // Initialize views
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        // Load emergency requests
        loadEmergencyRequests();
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
        swipeRefreshLayout.setOnRefreshListener(this::loadEmergencyRequests);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_red_dark,
            android.R.color.holo_red_light
        );
    }

    private void loadEmergencyRequests() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Query to get all emergency requests, ordered by priority and timestamp
        Query query = requestsRef.orderByChild("priorityLevel");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    EmergencyRequest request = requestSnapshot.getValue(EmergencyRequest.class);
                    if (request != null) {
                        requestList.add(request);
                    }
                }

                // Sort by priority (high to low) and timestamp (recent first)
                Collections.sort(requestList, (r1, r2) -> {
                    int priorityCompare = Integer.compare(r2.getPriorityLevel(), r1.getPriorityLevel());
                    if (priorityCompare != 0) return priorityCompare;
                    return r2.getTimestamp().compareTo(r1.getTimestamp());
                });

                adapter.updateList(requestList);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyRequestListActivity.this,
                        "Failed to load emergency requests", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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