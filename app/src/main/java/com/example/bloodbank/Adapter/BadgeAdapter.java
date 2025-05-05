package com.example.bloodbank.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
    private List<BadgeItem> badges = new ArrayList<>();

    public static class BadgeItem {
        private String name;
        private String description;
        private int iconResId;
        private boolean isUnlocked;

        public BadgeItem(String name, String description, int iconResId, boolean isUnlocked) {
            this.name = name;
            this.description = description;
            this.iconResId = iconResId;
            this.isUnlocked = isUnlocked;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getIconResId() { return iconResId; }
        public boolean isUnlocked() { return isUnlocked; }
    }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private ImageView badgeIcon;
        private TextView badgeName;
        private TextView badgeDescription;
        private View lockOverlay;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeIcon = itemView.findViewById(R.id.badgeIcon);
            badgeName = itemView.findViewById(R.id.badgeName);
            badgeDescription = itemView.findViewById(R.id.badgeDescription);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
        }

        public void bind(BadgeItem badge) {
            badgeIcon.setImageResource(badge.getIconResId());
            badgeName.setText(badge.getName());
            badgeDescription.setText(badge.getDescription());
            
            if (badge.isUnlocked()) {
                lockOverlay.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
            } else {
                lockOverlay.setVisibility(View.VISIBLE);
                itemView.setAlpha(0.5f);
            }
        }
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        holder.bind(badges.get(position));
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public void setBadges(List<BadgeItem> badges) {
        this.badges = badges;
        notifyDataSetChanged();
    }
} 