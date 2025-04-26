package com.example.bloodbank.Model;

import java.util.HashMap;
import java.util.Map;

public class DonorAchievement {
    private String donorId;
    private int totalPoints;
    private Map<String, Boolean> badges;
    private int donationStreak;
    private long lastDonationDate;
    private int rank;
    private String currentTitle;
    private Map<String, Long> achievementDates;

    public DonorAchievement() {
        // Required empty constructor for Firebase
        this.badges = new HashMap<>();
        this.achievementDates = new HashMap<>();
        this.totalPoints = 0;
        this.donationStreak = 0;
        this.rank = 0;
        this.currentTitle = "Novice Donor";
    }

    public DonorAchievement(String donorId) {
        this();
        this.donorId = donorId;
    }

    // Points system
    public void addPoints(int points) {
        this.totalPoints += points;
        updateTitle();
    }

    private void updateTitle() {
        if (totalPoints >= 1000) {
            currentTitle = "Legendary Lifesaver";
        } else if (totalPoints >= 500) {
            currentTitle = "Elite Donor";
        } else if (totalPoints >= 250) {
            currentTitle = "Regular Hero";
        } else if (totalPoints >= 100) {
            currentTitle = "Blood Champion";
        } else if (totalPoints >= 50) {
            currentTitle = "Active Donor";
        }
    }

    // Badge system
    public void unlockBadge(String badgeName) {
        badges.put(badgeName, true);
        achievementDates.put(badgeName, System.currentTimeMillis());
    }

    public boolean hasBadge(String badgeName) {
        return badges.containsKey(badgeName) && badges.get(badgeName);
    }

    // Streak system
    public void updateDonationStreak(long donationDate) {
        // If it's been more than 4 months since last donation, reset streak
        if (lastDonationDate > 0 &&
                (donationDate - lastDonationDate) > (120L * 24 * 60 * 60 * 1000)) {
            donationStreak = 1;
        } else {
            donationStreak++;
        }
        lastDonationDate = donationDate;
    }

    // Getters and Setters
    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
        updateTitle();
    }

    public Map<String, Boolean> getBadges() {
        return badges;
    }

    public void setBadges(Map<String, Boolean> badges) {
        this.badges = badges;
    }

    public int getDonationStreak() {
        return donationStreak;
    }

    public void setDonationStreak(int donationStreak) {
        this.donationStreak = donationStreak;
    }

    public long getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(long lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(String currentTitle) {
        this.currentTitle = currentTitle;
    }

    public Map<String, Long> getAchievementDates() {
        return achievementDates;
    }

    public void setAchievementDates(Map<String, Long> achievementDates) {
        this.achievementDates = achievementDates;
    }
}