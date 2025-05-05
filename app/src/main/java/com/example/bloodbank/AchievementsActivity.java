package com.example.bloodbank;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.Model.DonorAchievement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AchievementsActivity extends AppCompatActivity {
    private TextView donorTitleText;
    private TextView rankText;
    private TextView pointsText;
    private TextView streakText;
    private TextView nextTitleText;
    private TextView progressText;
    private RecyclerView badgesRecyclerView;
    private RecyclerView achievementsRecyclerView;
    private ProgressBar progressIndicator;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Achievements & Rewards");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initializeViews();

        // Load achievements data
        loadAchievements();
    }

    private void initializeViews() {
        donorTitleText = findViewById(R.id.donorTitleText);
        rankText = findViewById(R.id.rankText);
        pointsText = findViewById(R.id.pointsText);
        streakText = findViewById(R.id.streakText);
        nextTitleText = findViewById(R.id.nextTitleText);
        progressText = findViewById(R.id.progressText);
        badgesRecyclerView = findViewById(R.id.badgesRecyclerView);
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Set up RecyclerViews
        badgesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAchievements() {
        DocumentReference achievementRef = db.collection("donor_achievements").document(userId);
        achievementRef.get().addOnSuccessListener(documentSnapshot -> {
            DonorAchievement achievement = documentSnapshot.toObject(DonorAchievement.class);
            if (achievement != null) {
                updateUI(achievement);
            }
        });
    }

    private void updateUI(DonorAchievement achievement) {
        // Update donor status
        donorTitleText.setText(achievement.getCurrentTitle());
        rankText.setText("Community Rank: #" + achievement.getRank());
        pointsText.setText("Total Points: " + achievement.getTotalPoints());
        streakText.setText("Current Streak: " + achievement.getDonationStreak());

        // Calculate progress toward next title
        int currentPoints = achievement.getTotalPoints();
        int nextTitlePoints = getNextTitlePoints(currentPoints);
        int progress = (int) ((float) currentPoints / nextTitlePoints * 100);
        
        // Update progress bar
        progressIndicator.setProgress(progress);
        
        // Update next title text
        String nextTitle = getNextTitle(currentPoints);
        nextTitleText.setText("Next Title: " + nextTitle);
        
        // Update progress text
        progressText.setText(currentPoints + "/" + nextTitlePoints + " points");

        // Update badges
        updateBadges(achievement.getBadges());

        // Update recent achievements
        updateRecentAchievements(achievement.getAchievementDates());
    }

    private int getNextTitlePoints(int currentPoints) {
        if (currentPoints < 50) return 50;        // Next: Active Donor
        if (currentPoints < 100) return 100;      // Next: Blood Champion
        if (currentPoints < 250) return 250;      // Next: Regular Hero
        if (currentPoints < 500) return 500;      // Next: Elite Donor
        if (currentPoints < 1000) return 1000;    // Next: Legendary Lifesaver
        return 1000;                              // Max level reached
    }

    private String getNextTitle(int currentPoints) {
        if (currentPoints < 50) return "Active Donor";
        if (currentPoints < 100) return "Blood Champion";
        if (currentPoints < 250) return "Regular Hero";
        if (currentPoints < 500) return "Elite Donor";
        if (currentPoints < 1000) return "Legendary Lifesaver";
        return "Max Level Reached";
    }

    private void updateBadges(Map<String, Boolean> badges) {
        List<String> unlockedBadges = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : badges.entrySet()) {
            if (entry.getValue()) {
                unlockedBadges.add(entry.getKey());
            }
        }
        // TODO: Set up BadgesAdapter and update RecyclerView
    }

    private void updateRecentAchievements(Map<String, Long> achievementDates) {
        List<AchievementItem> recentAchievements = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        for (Map.Entry<String, Long> entry : achievementDates.entrySet()) {
            String achievement = entry.getKey();
            String date = dateFormat.format(new Date(entry.getValue()));
            recentAchievements.add(new AchievementItem(achievement, date));
        }

        // Sort by date (most recent first)
        recentAchievements.sort((a1, a2) -> Long.compare(
                achievementDates.get(a2.getName()),
                achievementDates.get(a1.getName())));

        // TODO: Set up AchievementsAdapter and update RecyclerView
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper class for achievement items
    private static class AchievementItem {
        private final String name;
        private final String date;

        public AchievementItem(String name, String date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }
    }
}