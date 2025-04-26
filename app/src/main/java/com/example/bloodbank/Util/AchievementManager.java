package com.example.bloodbank.Util;

import android.content.Context;

import com.example.bloodbank.Model.DonorAchievement;
import com.example.bloodbank.Model.DonorHealth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class AchievementManager {
    private static final int POINTS_PER_DONATION = 50;
    private static final int STREAK_BONUS = 20;
    private static final int EMERGENCY_DONATION_BONUS = 100;

    // Badge definitions
    public static final String BADGE_FIRST_DONATION = "First Blood";
    public static final String BADGE_REGULAR_DONOR = "Regular Donor";
    public static final String BADGE_STREAK_MASTER = "Streak Master";
    public static final String BADGE_EMERGENCY_HERO = "Emergency Hero";
    public static final String BADGE_MILESTONE_5 = "Blood Guardian";
    public static final String BADGE_MILESTONE_10 = "Blood Champion";
    public static final String BADGE_MILESTONE_25 = "Blood Legend";

    private final FirebaseFirestore db;
    private final Context context;

    public AchievementManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void processDonation(String donorId, boolean isEmergency) {
        DocumentReference achievementRef = db.collection("donor_achievements").document(donorId);
        DocumentReference healthRef = db.collection("donor_health").document(donorId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DonorAchievement achievement = transaction.get(achievementRef)
                    .toObject(DonorAchievement.class);
            DonorHealth health = transaction.get(healthRef)
                    .toObject(DonorHealth.class);

            if (achievement == null) {
                achievement = new DonorAchievement(donorId);
            }

            // Update basic points and streak
            achievement.addPoints(POINTS_PER_DONATION);
            achievement.updateDonationStreak(System.currentTimeMillis());

            // Add streak bonus if applicable
            if (achievement.getDonationStreak() > 1) {
                achievement.addPoints(STREAK_BONUS);
            }

            // Add emergency donation bonus
            if (isEmergency) {
                achievement.addPoints(EMERGENCY_DONATION_BONUS);
            }

            // Check and award badges
            checkAndAwardBadges(achievement, health);

            // Save changes
            transaction.set(achievementRef, achievement);
            return null;
        }).addOnSuccessListener(aVoid -> {
            NotificationHelper.sendEligibilityNotification(
                    context,
                    "Achievement Unlocked!",
                    "You've earned points and possibly new badges for your donation!");
        });
    }

    private void checkAndAwardBadges(DonorAchievement achievement, DonorHealth health) {
        // First donation badge
        if (health.getTotalDonations() == 1) {
            achievement.unlockBadge(BADGE_FIRST_DONATION);
        }

        // Regular donor badge (at least 3 donations)
        if (health.getTotalDonations() >= 3) {
            achievement.unlockBadge(BADGE_REGULAR_DONOR);
        }

        // Streak master badge (5+ donation streak)
        if (achievement.getDonationStreak() >= 5) {
            achievement.unlockBadge(BADGE_STREAK_MASTER);
        }

        // Milestone badges
        if (health.getTotalDonations() >= 5) {
            achievement.unlockBadge(BADGE_MILESTONE_5);
        }
        if (health.getTotalDonations() >= 10) {
            achievement.unlockBadge(BADGE_MILESTONE_10);
        }
        if (health.getTotalDonations() >= 25) {
            achievement.unlockBadge(BADGE_MILESTONE_25);
        }
    }

    public void processEmergencyDonation(String donorId) {
        DocumentReference achievementRef = db.collection("donor_achievements").document(donorId);

        achievementRef.get().addOnSuccessListener(documentSnapshot -> {
            DonorAchievement achievement = documentSnapshot.toObject(DonorAchievement.class);
            if (achievement == null) {
                achievement = new DonorAchievement(donorId);
            }

            // Award emergency hero badge
            achievement.unlockBadge(BADGE_EMERGENCY_HERO);
            achievement.addPoints(EMERGENCY_DONATION_BONUS);

            // Save changes
            achievementRef.set(achievement);

            NotificationHelper.sendEligibilityNotification(
                    context,
                    "Emergency Hero Badge Unlocked!",
                    "Thank you for responding to an emergency request!");
        });
    }

    public void updateRankings() {
        db.collection("donor_achievements")
                .orderBy("totalPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = 1;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        DonorAchievement achievement = doc.toObject(DonorAchievement.class);
                        if (achievement != null) {
                            achievement.setRank(rank++);
                            doc.getReference().set(achievement);
                        }
                    }
                });
    }
}