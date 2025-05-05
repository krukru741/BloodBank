package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.bloodbank.Adapter.UserAdapter;
import com.example.bloodbank.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectedActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recycleView;
    private List<User> userList;
    private UserAdapter userAdapter;
    private String title = "";
    private boolean isCompatibleMode = false;
    private String currentUserBloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selected);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycleView = findViewById(R.id.recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recycleView.setLayoutManager(linearLayoutManager);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(CategorySelectedActivity.this, userList);
        recycleView.setAdapter(userAdapter);

        if (getIntent().getExtras() != null) {
            title = getIntent().getStringExtra("group");
            isCompatibleMode = getIntent().getBooleanExtra("compatible", false);
            
            if (isCompatibleMode) {
                getSupportActionBar().setTitle("Compatible Users");
                fetchCurrentUserBloodGroup();
            } else {
                getSupportActionBar().setTitle("Blood group " + title);
                readUsers();
            }
        }
    }

    private void fetchCurrentUserBloodGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserBloodGroup = snapshot.child("bloodgroup").getValue(String.class);
                    if (currentUserBloodGroup != null) {
                        readCompatibleUsers();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategorySelectedActivity.this, 
                    "Error fetching your blood group", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readCompatibleUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && !user.getId().equals(currentUserId)) {
                        String donorBloodGroup = user.getBloodGroup();
                        if (isCompatible(donorBloodGroup)) {
                            userList.add(user);
                        }
                    }
                }

                if (userList.isEmpty()) {
                    Toast.makeText(CategorySelectedActivity.this, 
                        "No compatible users found", 
                        Toast.LENGTH_SHORT).show();
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategorySelectedActivity.this, 
                    "Error fetching users", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(CategorySelectedActivity.this, 
                        "User data not found", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("users");
                Query query = reference.orderByChild("bloodgroup").equalTo(title);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null && !user.getId().equals(currentUserId)) {
                                userList.add(user);
                            }
                        }
                        if (userList.isEmpty()) {
                            Toast.makeText(CategorySelectedActivity.this, 
                                "No users found with blood group " + title, 
                                Toast.LENGTH_SHORT).show();
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CategorySelectedActivity.this, 
                            "Error fetching users", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategorySelectedActivity.this, 
                    "Error fetching user data", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isCompatible(String donorBloodGroup) {
        if (currentUserBloodGroup == null || donorBloodGroup == null) {
            return false;
        }

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}