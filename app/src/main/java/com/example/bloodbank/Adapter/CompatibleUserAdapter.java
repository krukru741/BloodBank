package com.example.bloodbank.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bloodbank.Model.CompatibleUser;
import com.example.bloodbank.R;
import com.example.bloodbank.UserProfileActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompatibleUserAdapter extends RecyclerView.Adapter<CompatibleUserAdapter.ViewHolder> {
    private Context context;
    private List<CompatibleUser> userList;

    public CompatibleUserAdapter(Context context, List<CompatibleUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_displayed_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompatibleUser user = userList.get(position);

        // Set user data
        holder.nameText.setText(user.getName());
        holder.typeText.setText(user.getType());
        holder.bloodGroupText.setText(user.getBloodGroup());
        holder.emailText.setText(user.getEmail() != null ? user.getEmail() : "Email not available");
        holder.phoneText.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Phone not available");
        holder.addressText.setText(user.getAddress() != null ? user.getAddress() : "Address not available");
        holder.lastDonationText.setText(user.getLastDonation() != null ? 
            "Last Donation: " + user.getLastDonation() : "Last Donation: Not Available");

        // Load profile image
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePictureUrl())
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.profile);
        }

        // Set click listener for the email button
        holder.emailButton.setOnClickListener(v -> {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + user.getEmail()));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blood Donation Inquiry");
                context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
            } else {
                Toast.makeText(context, "Email not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the message button
        holder.messageButton.setOnClickListener(v -> {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("sms:" + user.getPhoneNumber()));
                context.startActivity(smsIntent);
            } else {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the profile image
        holder.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<CompatibleUser> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView nameText;
        private TextView typeText;
        private TextView bloodGroupText;
        private TextView emailText;
        private TextView phoneText;
        private TextView addressText;
        private TextView lastDonationText;
        private MaterialButton emailButton;
        private MaterialButton messageButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.userProfileImage);
            nameText = itemView.findViewById(R.id.userName);
            typeText = itemView.findViewById(R.id.type);
            bloodGroupText = itemView.findViewById(R.id.bloodGroup);
            emailText = itemView.findViewById(R.id.userEmail);
            phoneText = itemView.findViewById(R.id.phoneNumber);
            addressText = itemView.findViewById(R.id.address);
            lastDonationText = itemView.findViewById(R.id.lastDonation);
            emailButton = itemView.findViewById(R.id.emailNow);
            messageButton = itemView.findViewById(R.id.messageNow);
        }
    }
} 