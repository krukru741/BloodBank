package com.example.bloodbank;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonationCentersActivity extends AppCompatActivity {
    private RecyclerView centersRecyclerView;
    private DonationCentersAdapter adapter;
    private List<DonationCenter> centersList;
    private FirebaseFirestore db;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_centers);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Donation Centers");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        centersRecyclerView = findViewById(R.id.centersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fab = findViewById(R.id.fabAddDonationCenter);

        // Setup RecyclerView
        centersList = new ArrayList<>();
        adapter = new DonationCentersAdapter(centersList);
        centersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        centersRecyclerView.setAdapter(adapter);

        // Setup FAB click listener
        fab.setOnClickListener(v -> showAddDonationCenterDialog());

        // Load donation centers
        loadDonationCenters();
    }

    private void showAddDonationCenterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_donation_center, null);
        EditText nameInput = dialogView.findViewById(R.id.editTextName);
        EditText addressInput = dialogView.findViewById(R.id.editTextAddress);
        EditText phoneInput = dialogView.findViewById(R.id.editTextPhone);
        EditText emailInput = dialogView.findViewById(R.id.editTextEmail);
        Spinner citySpinner = dialogView.findViewById(R.id.spinnerCity);

        // Setup city spinner
        String[] cities = {"Manila", "Quezon City", "Makati", "Pasig", "Taguig", "Pasay", "Mandaluyong", "San Juan", "Marikina", "Las Piñas"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Donation Center")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String address = addressInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String city = citySpinner.getSelectedItem().toString();

                    if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addDonationCenter(name, address, phone, email, city);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addDonationCenter(String name, String address, String phone, String email, String city) {
        progressBar.setVisibility(View.VISIBLE);

        DonationCenter center = new DonationCenter();
        center.setName(name);
        center.setAddress(address);
        center.setPhone(phone);
        center.setEmail(email);
        center.setCity(city);
        center.setCreatedAt(System.currentTimeMillis());

        db.collection("donation_centers")
                .add(center)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Donation center added successfully", Toast.LENGTH_SHORT).show();
                    loadDonationCenters();  // Reload the list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error adding donation center: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadDonationCenters() {
        progressBar.setVisibility(View.VISIBLE);
        centersList.clear();

        db.collection("donation_centers")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error loading centers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    centersList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            DonationCenter center = doc.toObject(DonationCenter.class);
                            center.setId(doc.getId());
                            centersList.add(center);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 