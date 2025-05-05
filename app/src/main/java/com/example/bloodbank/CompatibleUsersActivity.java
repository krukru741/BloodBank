package com.example.bloodbank;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.Adapter.CompatibleUserAdapter;
import com.example.bloodbank.Model.CompatibleUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CompatibleUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CompatibleUserAdapter adapter;
    private List<CompatibleUser> userList;
    private ProgressBar progressBar;
    private TextView noUsersText;
    private DatabaseReference usersRef;
    private String currentUserBloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selected);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        noUsersText = findViewById(R.id.noUsersText);
        userList = new ArrayList<>();
        adapter = new CompatibleUserAdapter(this, userList);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Get current user's blood group
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // First, get current user's blood group
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserBloodGroup = snapshot.child("bloodGroup").getValue(String.class);
                    if (currentUserBloodGroup != null) {
                        fetchCompatibleUsers();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CompatibleUsers", "Error fetching current user data: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                noUsersText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchCompatibleUsers() {
        progressBar.setVisibility(View.VISIBLE);
        noUsersText.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && !userId.equals(currentUserId)) {
                        String bloodGroup = userSnapshot.child("bloodGroup").getValue(String.class);
                        String type = userSnapshot.child("type").getValue(String.class);
                        
                        if (bloodGroup != null && type != null && isCompatible(bloodGroup)) {
                            CompatibleUser user = new CompatibleUser(
                                userId,
                                userSnapshot.child("name").getValue(String.class),
                                type,
                                bloodGroup,
                                userSnapshot.child("profilePictureUrl").getValue(String.class),
                                userSnapshot.child("email").getValue(String.class),
                                userSnapshot.child("phoneNumber").getValue(String.class),
                                userSnapshot.child("address").getValue(String.class),
                                userSnapshot.child("lastDonation").getValue(String.class)
                            );
                            userList.add(user);
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                if (userList.isEmpty()) {
                    noUsersText.setVisibility(View.VISIBLE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    adapter.updateList(userList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CompatibleUsers", "Error fetching users: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                noUsersText.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isCompatible(String donorBloodGroup) {
        // Define blood group compatibility rules
        switch (currentUserBloodGroup) {
            case "A+":
                return donorBloodGroup.equals("A+") || donorBloodGroup.equals("A-") || 
                       donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "A-":
                return donorBloodGroup.equals("A-") || donorBloodGroup.equals("O-");
            case "B+":
                return donorBloodGroup.equals("B+") || donorBloodGroup.equals("B-") || 
                       donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "B-":
                return donorBloodGroup.equals("B-") || donorBloodGroup.equals("O-");
            case "AB+":
                return true; // AB+ can receive from all blood groups
            case "AB-":
                return donorBloodGroup.equals("A-") || donorBloodGroup.equals("B-") || 
                       donorBloodGroup.equals("AB-") || donorBloodGroup.equals("O-");
            case "O+":
                return donorBloodGroup.equals("O+") || donorBloodGroup.equals("O-");
            case "O-":
                return donorBloodGroup.equals("O-");
            default:
                return false;
        }
    }
} 