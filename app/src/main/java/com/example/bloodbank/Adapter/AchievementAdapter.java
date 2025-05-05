package com.example.bloodbank.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.R;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<AchievementItem> achievements = new ArrayList<>();

    public static class AchievementItem {
        private String name;
        private String description;
        private int iconResourceId;
        private int progress;
        private int maxProgress;
        private boolean isCompleted;

        public AchievementItem(String name, String description, int iconResourceId, int progress, int maxProgress) {
            this.name = name;
            this.description = description;
            this.iconResourceId = iconResourceId;
            this.progress = progress;
            this.maxProgress = maxProgress;
            this.isCompleted = progress >= maxProgress;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getIconResourceId() { return iconResourceId; }
        public int getProgress() { return progress; }
        public int getMaxProgress() { return maxProgress; }
        public boolean isCompleted() { return isCompleted; }
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private ImageView achievementIcon;
        private TextView achievementName;
        private TextView achievementDescription;
        private ProgressBar progressBar;
        private TextView progressText;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            achievementIcon = itemView.findViewById(R.id.achievementIcon);
            achievementName = itemView.findViewById(R.id.achievementName);
            achievementDescription = itemView.findViewById(R.id.achievementDescription);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
        }

        public void bind(AchievementItem achievement) {
            achievementIcon.setImageResource(achievement.getIconResourceId());
            achievementName.setText(achievement.getName());
            achievementDescription.setText(achievement.getDescription());
            
            progressBar.setMax(achievement.getMaxProgress());
            progressBar.setProgress(achievement.getProgress());
            
            String progressString = achievement.getProgress() + "/" + achievement.getMaxProgress();
            progressText.setText(progressString);

            // Adjust alpha based on completion status
            float alpha = achievement.isCompleted() ? 1.0f : 0.6f;
            itemView.setAlpha(alpha);
        }
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        holder.bind(achievements.get(position));
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void setAchievements(List<AchievementItem> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }
} 