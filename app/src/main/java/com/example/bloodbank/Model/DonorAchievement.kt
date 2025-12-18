package com.example.bloodbank.Model

import androidx.annotation.Keep

/**
 * Donor achievement data model for gamification system.
 * Tracks donor points, badges, streaks, and titles.
 */
@Keep
data class DonorAchievement(
    var donorId: String = "",
    var totalPoints: Int = 0,
    var badges: MutableMap<String, Boolean> = mutableMapOf(),
    var donationStreak: Int = 0,
    var lastDonationDate: Long = 0L,
    var rank: Int = 0,
    var currentTitle: String = "Novice Donor",
    var achievementDates: MutableMap<String, Long> = mutableMapOf()
) {
    // Required empty constructor for Firebase
    constructor() : this("", 0, mutableMapOf(), 0, 0L, 0, "Novice Donor", mutableMapOf())
    
    // Secondary constructor for creating with just donorId
    constructor(donorId: String) : this(
        donorId = donorId,
        totalPoints = 0,
        badges = mutableMapOf(),
        donationStreak = 0,
        lastDonationDate = 0L,
        rank = 0,
        currentTitle = "Novice Donor",
        achievementDates = mutableMapOf()
    )
    
    /**
     * Add points to the donor's total and update their title.
     */
    fun addPoints(points: Int) {
        totalPoints += points
        updateTitle()
    }
    
    /**
     * Update the donor's title based on their total points.
     */
    private fun updateTitle() {
        currentTitle = when {
            totalPoints >= 1000 -> "Legendary Lifesaver"
            totalPoints >= 500 -> "Elite Donor"
            totalPoints >= 250 -> "Regular Hero"
            totalPoints >= 100 -> "Blood Champion"
            totalPoints >= 50 -> "Active Donor"
            else -> "Novice Donor"
        }
    }
    
    /**
     * Unlock a badge for the donor.
     */
    fun unlockBadge(badgeName: String) {
        badges[badgeName] = true
        achievementDates[badgeName] = System.currentTimeMillis()
    }
    
    /**
     * Check if the donor has a specific badge.
     */
    fun hasBadge(badgeName: String): Boolean {
        return badges[badgeName] == true
    }
    
    /**
     * Update the donation streak based on the donation date.
     * Resets if more than 4 months have passed since last donation.
     */
    fun updateDonationStreak(donationDate: Long) {
        // If it's been more than 4 months since last donation, reset streak
        val fourMonthsInMillis = 120L * 24 * 60 * 60 * 1000
        donationStreak = if (lastDonationDate > 0 && (donationDate - lastDonationDate) > fourMonthsInMillis) {
            1
        } else {
            donationStreak + 1
        }
        lastDonationDate = donationDate
    }
    
    /**
     * Custom setter for totalPoints that updates the title.
     */
    fun setTotalPointsWithUpdate(points: Int) {
        totalPoints = points
        updateTitle()
    }
}
