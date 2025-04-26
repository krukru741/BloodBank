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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Adapter.UserAdapter;
import com.example.bloodbank.Model.User;
import com.example.bloodbank.Service.DonationReminderService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    private CircleImageView nav_profile_image;

    private DatabaseReference userRef;

    private RecyclerView recycleView;
    private ProgressBar progressbar;

    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blood Donation App");

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("type").getValue().toString();
                if (type.equals("donor")) {
                    readRecipients();
                } else {
                    readDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        nav_profile_image = nav_view.getHeaderView(0).findViewById(R.id.nav_user_image);
        nav_fullname = nav_view.getHeaderView(0).findViewById(R.id.nav_user_fullname);
        nav_email = nav_view.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_bloodgroup = nav_view.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);
        nav_type = nav_view.getHeaderView(0).findViewById(R.id.nav_user_type);

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    nav_fullname.setText(name);

                    String email = snapshot.child("email").getValue().toString();
                    nav_email.setText(email);

                    String bloodgroup = snapshot.child("bloodgroup").getValue().toString();
                    nav_bloodgroup.setText(bloodgroup);

                    String type = snapshot.child("type").getValue().toString();
                    nav_type.setText(type);

                    // Show FAB only for recipients
                    fabEmergency.setVisibility(type.equals("recipient") ? View.VISIBLE : View.GONE);

                    if (snapshot.hasChild("profilepictureurl")) {
                        String imageUrl = snapshot.child("profilepictureurl").getValue().toString();
                        Glide.with(getApplicationContext()).load(imageUrl).into(nav_profile_image);
                    } else {
                        nav_profile_image.setImageResource(R.drawable.profile);
                    }

                    Menu nav_menu = nav_view.getMenu();

                    if (type.equals("donor")) {
                        nav_menu.findItem(R.id.sentEmail).setTitle("Received Emails");
                        nav_menu.findItem(R.id.notifications).setVisible(true);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Set up FAB visibility and click listener
        fabEmergency.setVisibility(View.GONE); // Hide by default
        fabEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmergencyRequest();
            }
        });

    }

    private void readDonors() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("donor");
        query.addValueEventListener(new ValueEventListener() {
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

            }
        });
    }

    private void readRecipients() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("recipient");
        query.addValueEventListener(new ValueEventListener() {
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

            }
        });
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
        } else if (itemId == R.id.aplus) {
            Intent intent2 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent2.putExtra("group", "A+");
            startActivity(intent2);
        } else if (itemId == R.id.aminus) {
            Intent intent3 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent3.putExtra("group", "A-");
            startActivity(intent3);
        } else if (itemId == R.id.bplus) {
            Intent intent4 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent4.putExtra("group", "B+");
            startActivity(intent4);
        } else if (itemId == R.id.bminus) {
            Intent intent5 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent5.putExtra("group", "B-");
            startActivity(intent5);
        } else if (itemId == R.id.abplus) {
            Intent intent6 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent6.putExtra("group", "AB+");
            startActivity(intent6);
        } else if (itemId == R.id.abminus) {
            Intent intent7 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent7.putExtra("group", "AB-");
            startActivity(intent7);
        } else if (itemId == R.id.oplus) {
            Intent intent8 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent8.putExtra("group", "O+");
            startActivity(intent8);
        } else if (itemId == R.id.ominus) {
            Intent intent9 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent9.putExtra("group", "O-");
            startActivity(intent9);
        } else if (itemId == R.id.compatible) {
            Intent intent10 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            startActivity(intent10);
        } else if (itemId == R.id.notifications) {
            Intent intent12 = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent12);
        } else if (itemId == R.id.aboutus) {
            Intent intent13 = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent13);
        } else if (itemId == R.id.Faq) {
            Intent intent14 = new Intent(MainActivity.this, FaqActivity.class);
            startActivity(intent14);
        } else if (itemId == R.id.sentEmail) {
            Intent intent11 = new Intent(MainActivity.this, SentEmailActivity.class);
            startActivity(intent11);
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent1);
        } else if (itemId == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.donor_health) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String type = snapshot.child("type").getValue().toString();
                        if (type.equals("donor")) {
                            startActivity(new Intent(MainActivity.this, DonorHealthActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Only donors can access health tracking", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (itemId == R.id.schedule_donation) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String type = snapshot.child("type").getValue().toString();
                        if (type.equals("donor")) {
                            startActivity(new Intent(MainActivity.this, DonationSchedulingActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Only donors can schedule donations", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (itemId == R.id.my_appointments) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String type = snapshot.child("type").getValue().toString();
                        if (type.equals("donor")) {
                            startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Only donors can view appointments", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (itemId == R.id.donation_centers) {
            startActivity(new Intent(MainActivity.this, DonationCentersActivity.class));
        } else if (itemId == R.id.achievements) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String type = snapshot.child("type").getValue().toString();
                        if (type.equals("donor")) {
                            startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Only donors can view achievements", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
}