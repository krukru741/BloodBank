package com.example.bloodbank.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Model.Notification;
import com.example.bloodbank.Model.User;
import com.example.bloodbank.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        // Get user details
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(notification.getUserId());
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        holder.username.setText(user.getName());

                        // Load profile image
                        if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                            Glide.with(context)
                                    .load(new java.io.File(user.getProfileImagePath()))
                                    .placeholder(R.drawable.profile_pic)
                                    .error(R.drawable.profile_pic)
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.profile_pic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently
            }
        });
        
        holder.message.setText(notification.getMessage());
        holder.time.setText(notification.getTime());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView username, message, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.notification_profile_image);
            username = itemView.findViewById(R.id.notification_name);
            message = itemView.findViewById(R.id.notification_text);
            time = itemView.findViewById(R.id.notification_date);
        }
    }
}
