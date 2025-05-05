package com.example.bloodbank.Adapter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bloodbank.ChatActivity;
import com.example.bloodbank.Email.JavaMailApi;
import com.example.bloodbank.LoginActivity;
import com.example.bloodbank.Model.User;
import com.example.bloodbank.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private boolean isChat;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.isChat = false;
    }

    public UserAdapter(Context context, List<User> userList, boolean isChat) {
        this.context = context;
        this.userList = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_displayed_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);
        
        // Debug logging
        android.util.Log.d("UserAdapter", "Binding user: " + user.getName());
        android.util.Log.d("UserAdapter", "Profile URL: " + user.getProfileImagePath());
        android.util.Log.d("UserAdapter", "Phone Number: " + user.getPhoneNumber());
        android.util.Log.d("UserAdapter", "Blood Group: " + user.getBloodGroup());

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return;
        }

        // Set basic user information
        holder.username.setText(user.getName());
        holder.type.setText(user.getType());
        
        // Set blood group with proper formatting
        if (user.getBloodGroup() != null && !user.getBloodGroup().isEmpty()) {
            holder.bloodGroup.setText(user.getBloodGroup());
            android.util.Log.d("UserAdapter", "Setting blood group: " + user.getBloodGroup());
        } else {
            holder.bloodGroup.setText("Blood Group: Not Available");
            android.util.Log.d("UserAdapter", "No blood group available");
        }
        
        // Set contact information with better visibility handling
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            holder.userEmail.setVisibility(View.VISIBLE);
            holder.userEmail.setText(user.getEmail());
        } else {
            holder.userEmail.setText("Email not provided");
        }

        // Handle phone number display
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            holder.phoneNumber.setVisibility(View.VISIBLE);
            holder.phoneNumber.setText(user.getPhoneNumber());
            android.util.Log.d("UserAdapter", "Setting phone number: " + user.getPhoneNumber());
        } else {
            holder.phoneNumber.setText("Phone not provided");
            android.util.Log.d("UserAdapter", "No phone number available");
        }

        // Handle address display
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            holder.address.setVisibility(View.VISIBLE);
            holder.address.setText(user.getAddress());
        } else {
            holder.address.setText("Address not provided");
        }

        // Handle last donation display
        if (user.getLastDonationDate() != null && !user.getLastDonationDate().isEmpty()) {
            holder.lastDonation.setVisibility(View.VISIBLE);
            holder.lastDonation.setText("Last Donation: " + user.getLastDonationDate());
        } else {
            holder.lastDonation.setText("Last Donation: Not Available");
        }
        
        // Load profile image with enhanced error handling for local storage
        try {
            String profileImagePath = user.getProfileImagePath();
            if (profileImagePath != null && !profileImagePath.isEmpty()) {
                android.util.Log.d("UserAdapter", "Loading image from local path: " + profileImagePath);
                
                // Create file object from local storage path
                java.io.File imageFile = new java.io.File(profileImagePath);
                if (imageFile.exists()) {
                    Glide.with(context)
                            .load(imageFile)
                            .placeholder(R.drawable.profile_pic)
                            .error(R.drawable.profile_pic)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    android.util.Log.e("UserAdapter", "Failed to load image: " + (e != null ? e.getMessage() : "unknown error"));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    android.util.Log.d("UserAdapter", "Image loaded successfully from: " + profileImagePath);
                                    return false;
                                }
                            })
                            .into(holder.profileImage);
                } else {
                    // Try to find the image in the app's internal storage
                    java.io.File storageDir = new java.io.File(context.getFilesDir(), "BloodBank/ProfileImages");
                    if (storageDir.exists()) {
                        java.io.File[] files = storageDir.listFiles();
                        if (files != null) {
                            for (java.io.File file : files) {
                                if (file.getName().equals(imageFile.getName())) {
                                    Glide.with(context)
                                            .load(file)
                                            .placeholder(R.drawable.profile_pic)
                                            .error(R.drawable.profile_pic)
                                            .into(holder.profileImage);
                                    return;
                                }
                            }
                        }
                    }
                    android.util.Log.e("UserAdapter", "Image file does not exist at path: " + profileImagePath);
                    holder.profileImage.setImageResource(R.drawable.profile_pic);
                }
            } else {
                android.util.Log.d("UserAdapter", "No profile image path available");
                holder.profileImage.setImageResource(R.drawable.profile_pic);
            }
        } catch (Exception e) {
            android.util.Log.e("UserAdapter", "Error loading image: " + e.getMessage());
            e.printStackTrace();
            holder.profileImage.setImageResource(R.drawable.profile_pic);
        }
        
        if (isChat) {
            holder.lastMessage.setVisibility(View.VISIBLE);
            holder.btnEmail.setVisibility(View.GONE);
            holder.btnMessage.setVisibility(View.GONE);
            
            // Get last message
            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(user.getId());
            
            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String lastMsg = snapshot.child("lastMessage").getValue().toString();
                        String lastMsgTime = snapshot.child("lastMessageTime").getValue().toString();
                        
                        holder.lastMessage.setText(lastMsg);
                    } else {
                        holder.lastMessage.setText("No messages yet");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error silently
                }
            });
            
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userid", user.getId());
                    intent.putExtra("profilepic", user.getProfileImagePath());
                    intent.putExtra("username", user.getName());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.lastMessage.setVisibility(View.GONE);
            holder.btnEmail.setVisibility(View.VISIBLE);
            holder.btnMessage.setVisibility(View.VISIBLE);
            
            holder.btnEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if user is logged in
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        // User is not logged in, redirect to login
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        return;
                    }
                    
                    // Send email
                    sendEmail(user);
                }
            });
            
            holder.btnMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if user is logged in
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        // User is not logged in, redirect to login
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        return;
                    }
                    
                    // Open chat activity
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userid", user.getId());
                    intent.putExtra("profilepic", user.getProfileImagePath());
                    intent.putExtra("username", user.getName());
                    context.startActivity(intent);
                }
            });
        }
    }
    
    private void sendEmail(User user) {
        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return;
        }
        
        // Get current user
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User currentUser = snapshot.getValue(User.class);
                    if (currentUser != null) {
                        // Create email intent
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + user.getEmail()));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blood Donation Inquiry");
                        
                        // Create email body with user information
                        String emailBody = "Hello " + user.getName() + ",\n\n" +
                                "I am " + currentUser.getName() + " and I am interested in your blood donation.\n" +
                                "My blood group is " + currentUser.getBloodGroup() + ".\n" +
                                "Please let me know if you are available for donation.\n\n" +
                                "Best regards,\n" +
                                currentUser.getName();
                        
                        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
                        
                        // Save email to database
                        DatabaseReference emailsRef = FirebaseDatabase.getInstance().getReference().child("emails")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(user.getId());
                        
                        emailsRef.setValue(true)
                                .addOnSuccessListener(aVoid -> {
                                    // Start email client
                                    try {
                                        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView bloodGroup;
        public TextView type;
        public TextView lastMessage;
        public CircleImageView profileImage;
        public Button btnEmail;
        public Button btnMessage;
        public TextView userEmail;
        public TextView phoneNumber;
        public TextView address;
        public TextView lastDonation;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userName);
            bloodGroup = itemView.findViewById(R.id.bloodGroup);
            type = itemView.findViewById(R.id.type);
            profileImage = itemView.findViewById(R.id.userProfileImage);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            btnEmail = itemView.findViewById(R.id.emailNow);
            btnMessage = itemView.findViewById(R.id.messageNow);
            userEmail = itemView.findViewById(R.id.userEmail);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            address = itemView.findViewById(R.id.address);
            lastDonation = itemView.findViewById(R.id.lastDonation);
        }
    }

    private void addNotifications(String receiverId, String senderId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("notifications").child(receiverId);
        
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("text", "Sent you an email. Kindly check it out!");
        hashMap.put("date", date);
        hashMap.put("isRead", false);

        reference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    // Don't show error to user, just log it
                    System.out.println("Failed to send notification: " + task.getException().getMessage());
                }
            }
        });
    }
}
