package com.example.bloodbank;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bloodbank.adapters.AppointmentsAdapter;
import com.example.bloodbank.models.Donation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {
    private RecyclerView appointmentsRecyclerView;
    private AppointmentsAdapter adapter;
    private List<Donation> appointmentsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View progressBar;
    private TextView emptyView;
    private DatabaseReference donationsRef;
    private String userId;
    private ValueEventListener appointmentsListener;
    private Query appointmentsQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        donationsRef = FirebaseDatabase.getInstance().getReference().child("donations");

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        appointmentsList = new ArrayList<>();
        adapter = new AppointmentsAdapter(appointmentsList);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentsRecyclerView.setAdapter(adapter);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadAppointments);
        
        // Load appointments
        loadAppointments();
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Appointments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        // Remove previous listener if exists
        if (appointmentsListener != null && appointmentsQuery != null) {
            appointmentsQuery.removeEventListener(appointmentsListener);
        }
        
        // Query donations for the current user
        appointmentsQuery = donationsRef.orderByChild("donorId").equalTo(userId);
        
        appointmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appointmentsList.clear();
                
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Donation donation = snapshot.getValue(Donation.class);
                        if (donation != null) {
                            donation.setDonationId(snapshot.getKey());
                            appointmentsList.add(donation);
                        }
                    }
                }
                
                // Sort appointments by date (newest first)
                appointmentsList.sort((d1, d2) -> Long.compare(d2.getTimestamp(), d1.getTimestamp()));
                
                // Update UI
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                // Show empty view if no appointments
                if (appointmentsList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MyAppointmentsActivity.this, 
                        "Error loading appointments: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        };
        
        // Add the listener
        appointmentsQuery.addValueEventListener(appointmentsListener);
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
        if (appointmentsListener != null && appointmentsQuery != null) {
            appointmentsQuery.removeEventListener(appointmentsListener);
        }
    }
} 