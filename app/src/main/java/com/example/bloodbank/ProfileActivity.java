package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView type, name, gender, idNumber, phoneNumber, bloodGroup, address, birthdate, email;
    private CircleImageView profileImage;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileImage = findViewById(R.id.profileImage);
        type = findViewById(R.id.type);
        name = findViewById(R.id.name);
        gender = findViewById(R.id.gender);
        idNumber = findViewById(R.id.idNumber);
        phoneNumber = findViewById(R.id.phoneNumber);
        bloodGroup = findViewById(R.id.bloodGroup);
        address = findViewById(R.id.address);
        birthdate = findViewById(R.id.birthdate);
        email = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    type.setText(snapshot.child("type").getValue().toString());
                    name.setText(snapshot.child("name").getValue().toString());
                    gender.setText(snapshot.child("gender").getValue().toString());
                    idNumber.setText(snapshot.child("idnumber").getValue().toString());
                    phoneNumber.setText(snapshot.child("phonenumber").getValue().toString());
                    bloodGroup.setText(snapshot.child("bloodgroup").getValue().toString());
                    address.setText(snapshot.child("address").getValue().toString());
                    birthdate.setText(snapshot.child("birthdate").getValue().toString());
                    email.setText(snapshot.child("email").getValue().toString());

                    Glide.with(getApplicationContext()).load(snapshot.child("profilepictureurl").getValue().toString()).into(profileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}