package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.bloodbank.Adapter.MessageAdapter;
import com.example.bloodbank.Model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private Toolbar toolbar;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String userId;
    private String profilePic;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get user info from intent
        userId = getIntent().getStringExtra("userid");
        profilePic = getIntent().getStringExtra("profilepic");
        userName = getIntent().getStringExtra("username");

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load messages
        loadMessages();
        
        // Setup send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText("");
                }
            }
        });
    }
    
    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.username);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        toolbar = findViewById(R.id.toolbar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        username.setText(userName);
        if (profilePic != null && !profilePic.isEmpty()) {
            // Load profile image using Glide
            com.bumptech.glide.Glide.with(this)
                    .load(profilePic)
                    .placeholder(R.drawable.profile_pic)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.profile_pic);
        }
    }
    
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
    }
    
    private void loadMessages() {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                                message.getReceiverId().equals(userId) ||
                                message.getSenderId().equals(userId) &&
                                        message.getReceiverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            messageList.add(message);
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently
            }
        });
    }
    
    private void sendMessage(String messageText) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        String pushId = chatRef.push().getKey();
        
        if (pushId != null) {
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            messageMap.put("receiverId", userId);
            messageMap.put("message", messageText);
            messageMap.put("timestamp", System.currentTimeMillis());
            
            chatRef.child(pushId).setValue(messageMap);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 