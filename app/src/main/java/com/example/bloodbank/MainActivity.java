package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Adapter.UserAdapter;
import com.example.bloodbank.Model.User;
import com.example.bloodbank.Service.DonationReminderService;
import com.example.bloodbank.utils.GooglePlayServicesUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView nav_view;
    private FloatingActionButton fabEmergency;

    private TextView nav_fullname, nav_email, nav_bloodgroup, nav_type;
    private TextView nav_last_donation, nav_last_request;
    private TextView nav_phone;
    private LinearLayout additional_info_section, donor_info, recipient_info;
    private CircleImageView nav_profile_image;

    private DatabaseReference userRef;
    private ValueEventListener userValueEventListener;
    private ValueEventListener userTypeValueEventListener;
    private ValueEventListener donorsQueryListener;
    private ValueEventListener recipientsQueryListener;

    private RecyclerView recycleView;
    private ProgressBar progressbar;

    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for Google Play Services
        checkGooglePlayServices();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        nav_view = findViewById(R.id.nav_view);
        fabEmergency = findViewById(R.id.fab_emergency);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);

        progressbar = findViewById(R.id.progressbar);

        recycleView = findViewById(R.id.recycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycleView.setLayoutManager(layoutManager);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(MainActivity.this, userList);

        recycleView.setAdapter(userAdapter);

        // Initialize Firebase references
        initializeFirebaseListeners();
    }

    private void initializeFirebaseListeners() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Reference to user data
        userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);
        
        // Listener for user type
        userTypeValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("type").getValue().toString();
                if (type.equals("donor")) {
                    readRecipients();
                    showDonorInfo();
                } else {
                    readDonors();
                    showRecipientInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        
        // Add the listener
        userRef.addValueEventListener(userTypeValueEventListener);
        
        // Initialize navigation header views
        View headerView = nav_view.getHeaderView(0);
        if (headerView != null) {
            nav_profile_image = headerView.findViewById(R.id.nav_user_image);
            nav_fullname = headerView.findViewById(R.id.nav_user_fullname);
            nav_email = headerView.findViewById(R.id.nav_user_email);
            nav_phone = headerView.findViewById(R.id.nav_user_phone);
            nav_bloodgroup = headerView.findViewById(R.id.nav_user_bloodgroup);
            nav_type = headerView.findViewById(R.id.nav_user_type);
            
            // Initialize additional info views
            additional_info_section = headerView.findViewById(R.id.additional_info_section);
            donor_info = headerView.findViewById(R.id.donor_info);
            recipient_info = headerView.findViewById(R.id.recipient_info);
            nav_last_donation = headerView.findViewById(R.id.nav_last_donation);
            nav_last_request = headerView.findViewById(R.id.nav_last_request);
        }

        // Listener for user profile data
        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Update basic user info
                    String fullname = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phonenumber").getValue(String.class);
                    String bloodgroup = snapshot.child("bloodgroup").getValue(String.class);
                    String type = snapshot.child("type").getValue(String.class);

                    // Log the values for debugging
                    android.util.Log.d("MainActivity", "Fullname: " + fullname);
                    android.util.Log.d("MainActivity", "Email: " + email);
                    android.util.Log.d("MainActivity", "Phone: " + phone);
                    android.util.Log.d("MainActivity", "Bloodgroup: " + bloodgroup);
                    android.util.Log.d("MainActivity", "Type: " + type);

                    if (fullname != null && nav_fullname != null) {
                        nav_fullname.setText(fullname);
                    } else {
                        android.util.Log.e("MainActivity", "Fullname is null or nav_fullname is null");
                    }
                    if (email != null && nav_email != null) {
                        nav_email.setText(email);
                    }
                    if (phone != null && nav_phone != null) {
                        nav_phone.setText(phone);
                    }
                    if (bloodgroup != null && nav_bloodgroup != null) {
                        nav_bloodgroup.setText(bloodgroup);
                    }
                    if (type != null && nav_type != null) {
                        nav_type.setText(type.toUpperCase());
                        if (type.equals("donor")) {
                            showDonorInfo();
                            if (snapshot.hasChild("lastDonation")) {
                                String lastDonation = snapshot.child("lastDonation").getValue(String.class);
                                if (lastDonation != null && nav_last_donation != null) {
                                    nav_last_donation.setText(lastDonation);
                                }
                            }
                        } else {
                            showRecipientInfo();
                            if (snapshot.hasChild("lastRequest")) {
                                String lastRequest = snapshot.child("lastRequest").getValue(String.class);
                                if (lastRequest != null && nav_last_request != null) {
                                    nav_last_request.setText(lastRequest);
                                }
                            }
                        }
                    }

                    // Update profile image if available
                    if (snapshot.hasChild("profileImagePath") && nav_profile_image != null) {
                        String profileImagePath = snapshot.child("profileImagePath").getValue(String.class);
                        if (profileImagePath != null && !profileImagePath.isEmpty()) {
                            try {
                                File imgFile = new File(profileImagePath);
                                if (imgFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                    nav_profile_image.setImageBitmap(bitmap);
                                }
                            } catch (Exception e) {
                                android.util.Log.e("MainActivity", "Error loading profile image: " + e.getMessage());
                                nav_profile_image.setImageResource(R.drawable.profile);
                            }
                        }
                    }
                } else {
                    android.util.Log.e("MainActivity", "Snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("MainActivity", "Error loading user data: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Error loading user data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };
        
        userRef.addValueEventListener(userValueEventListener);

        // Set up FAB visibility and click listener
        fabEmergency.setVisibility(View.GONE); // Hide by default
        fabEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmergencyRequest();
            }
        });
    }

    private void showDonorInfo() {
        additional_info_section.setVisibility(View.VISIBLE);
        donor_info.setVisibility(View.VISIBLE);
        recipient_info.setVisibility(View.GONE);
    }

    private void showRecipientInfo() {
        additional_info_section.setVisibility(View.VISIBLE);
        donor_info.setVisibility(View.GONE);
        recipient_info.setVisibility(View.VISIBLE);
    }

    private void readDonors() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("donor");
        
        donorsQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No Recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        
        query.addValueEventListener(donorsQueryListener);
    }

    private void readRecipients() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("recipient");
        
        recipientsQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No Recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        
        query.addValueEventListener(recipientsQueryListener);
    }

    private void startEmergencyRequest() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue().toString();
                    Intent intent;
                    if (type.equals("recipient")) {
                        intent = new Intent(MainActivity.this, EmergencyRequestActivity.class);
                    } else {
                        intent = new Intent(MainActivity.this, EmergencyRequestListActivity.class);
                    }
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.emergency_request) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String type = snapshot.child("type").getValue().toString();
                        Intent intent;
                        if (type.equals("recipient")) {
                            intent = new Intent(MainActivity.this, EmergencyRequestActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, EmergencyRequestListActivity.class);
                        }
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else if (itemId == R.id.view_emergency_requests) {
            startActivity(new Intent(MainActivity.this, EmergencyRequestListActivity.class));
        } else if (itemId == R.id.donor_health) {
            startActivity(new Intent(MainActivity.this, DonorHealthActivity.class));
        } else if (itemId == R.id.achievements) {
            startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
        } else if (itemId == R.id.schedule_donation) {
            startActivity(new Intent(MainActivity.this, ScheduleDonationActivity.class));
        } else if (itemId == R.id.my_appointments) {
            startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
        } else if (itemId == R.id.donation_centers) {
            startActivity(new Intent(MainActivity.this, DonationCentersActivity.class));
        } else if (itemId == R.id.aplus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "A+");
            startActivity(intent);
        } else if (itemId == R.id.aminus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "A-");
            startActivity(intent);
        } else if (itemId == R.id.bplus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "B+");
            startActivity(intent);
        } else if (itemId == R.id.bminus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "B-");
            startActivity(intent);
        } else if (itemId == R.id.abplus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "AB+");
            startActivity(intent);
        } else if (itemId == R.id.abminus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "AB-");
            startActivity(intent);
        } else if (itemId == R.id.oplus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "O+");
            startActivity(intent);
        } else if (itemId == R.id.ominus) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent.putExtra("group", "O-");
            startActivity(intent);
        } else if (itemId == R.id.compatible) {
            Intent intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.notifications) {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.aboutus) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.Faq) {
            Intent intent = new Intent(MainActivity.this, FaqActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.sentEmail) {
            Intent intent = new Intent(MainActivity.this, SentEmailActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.logout) {
            logout();
        } else if (itemId == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Properly handle logout by removing all Firebase listeners before signing out
     */
    private void logout() {
        // Remove all Firebase listeners
        if (userRef != null) {
            if (userValueEventListener != null) {
                userRef.removeEventListener(userValueEventListener);
            }
            if (userTypeValueEventListener != null) {
                userRef.removeEventListener(userTypeValueEventListener);
            }
        }
        
        // Remove query listeners
        if (donorsQueryListener != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .orderByChild("type")
                    .equalTo("donor")
                    .removeEventListener(donorsQueryListener);
        }
        
        if (recipientsQueryListener != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .orderByChild("type")
                    .equalTo("recipient")
                    .removeEventListener(recipientsQueryListener);
        }
        
        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();
        
        // Navigate to login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check donation eligibility for donors
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue().toString();
                    if (type.equals("donor")) {
                        new DonationReminderService(MainActivity.this).checkAndUpdateEligibility();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    /**
     * Check if Google Play Services is available and show update dialog if needed
     */
    private void checkGooglePlayServices() {
        if (!GooglePlayServicesUtils.isGooglePlayServicesAvailable(this)) {
            GooglePlayServicesUtils.showGooglePlayServicesUpdateDialog(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Play Services resolution result
        if (requestCode == 9000) {
            if (resultCode == RESULT_OK) {
                // Google Play Services is now available
                Toast.makeText(this, "Google Play Services is now available", Toast.LENGTH_SHORT).show();
            } else {
                // Google Play Services is still not available
                Toast.makeText(this, "Google Play Services is required for this app", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners when activity is destroyed
        if (userRef != null) {
            if (userValueEventListener != null) {
                userRef.removeEventListener(userValueEventListener);
            }
            if (userTypeValueEventListener != null) {
                userRef.removeEventListener(userTypeValueEventListener);
            }
        }
    }
}