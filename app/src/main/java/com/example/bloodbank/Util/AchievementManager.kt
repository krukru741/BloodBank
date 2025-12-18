package com.example.bloodbank.Util

import android.content.Context
import com.example.bloodbank.Model.DonorAchievement
import com.example.bloodbank.Model.DonorHealth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * AchievementManager - Manages donor achievements, badges, and rankings.
 * Uses dependency injection for Context and Firestore.
 */
class AchievementManager(private val context: Context) {
    
    private val db = FirebaseFirestore.getInstance()
    
    companion object {
        private const val POINTS_PER_DONATION = 50
        private const val STREAK_BONUS = 20
        private const val EMERGENCY_DONATION_BONUS = 100
        
        // Badge definitions
        const val BADGE_FIRST_DONATION = "First Blood"
        const val BADGE_REGULAR_DONOR = "Regular Donor"
        const val BADGE_STREAK_MASTER = "Streak Master"
        const val BADGE_EMERGENCY_HERO = "Emergency Hero"
        const val BADGE_MILESTONE_5 = "Blood Guardian"
        const val BADGE_MILESTONE_10 = "Blood Champion"
        const val BADGE_MILESTONE_25 = "Blood Legend"
    }
    
    /**
     * Process a donation and award points/badges.
     */
    fun processDonation(donorId: String, isEmergency: Boolean) {
        val achievementRef = db.collection("donor_achievements").document(donorId)
        val healthRef = db.collection("donor_health").document(donorId)
        
        db.runTransaction { transaction ->
            val achievement = transaction.get(achievementRef)
                .toObject(DonorAchievement::class.java)
                ?: DonorAchievement(donorId)
            
            val health = transaction.get(healthRef)
                .toObject(DonorHealth::class.java)
            
            // Update basic points and streak
            achievement.addPoints(POINTS_PER_DONATION)
            achievement.updateDonationStreak(System.currentTimeMillis())
            
            // Add streak bonus if applicable
            if (achievement.donationStreak > 1) {
                achievement.addPoints(STREAK_BONUS)
            }
            
            // Add emergency donation bonus
            if (isEmergency) {
                achievement.addPoints(EMERGENCY_DONATION_BONUS)
            }
            
            // Check and award badges
            checkAndAwardBadges(achievement, health)
            
            // Save changes
            transaction.set(achievementRef, achievement)
            null
        }.addOnSuccessListener {
            NotificationHelper.sendEligibilityNotification(
                context,
                "Achievement Unlocked!",
                "You've earned points and possibly new badges for your donation!"
            )
        }
    }
    
    /**
     * Check donation milestones and award appropriate badges.
     */
    private fun checkAndAwardBadges(achievement: DonorAchievement, health: DonorHealth?) {
        health?.let {
            // First donation badge
            if (it.totalDonations == 1) {
                achievement.unlockBadge(BADGE_FIRST_DONATION)
            }
            
            // Regular donor badge (at least 3 donations)
            if (it.totalDonations >= 3) {
                achievement.unlockBadge(BADGE_REGULAR_DONOR)
            }
            
            // Milestone badges
            when {
                it.totalDonations >= 25 -> achievement.unlockBadge(BADGE_MILESTONE_25)
                it.totalDonations >= 10 -> achievement.unlockBadge(BADGE_MILESTONE_10)
                it.totalDonations >= 5 -> achievement.unlockBadge(BADGE_MILESTONE_5)
            }
        }
        
        // Streak master badge (5+ donation streak)
        if (achievement.donationStreak >= 5) {
            achievement.unlockBadge(BADGE_STREAK_MASTER)
        }
    }
    
    /**
     * Process emergency donation and award emergency hero badge.
     */
    fun processEmergencyDonation(donorId: String) {
        val achievementRef = db.collection("donor_achievements").document(donorId)
        
        achievementRef.get().addOnSuccessListener { documentSnapshot ->
            val achievement = documentSnapshot.toObject(DonorAchievement::class.java)
                ?: DonorAchievement(donorId)
            
            // Award emergency hero badge
            achievement.unlockBadge(BADGE_EMERGENCY_HERO)
            achievement.addPoints(EMERGENCY_DONATION_BONUS)
            
            // Save changes
            achievementRef.set(achievement)
            
            NotificationHelper.sendEligibilityNotification(
                context,
                "Emergency Hero Badge Unlocked!",
                "Thank you for responding to an emergency request!"
            )
        }
    }
    
    /**
     * Update global donor rankings based on total points.
     */
    fun updateRankings() {
        db.collection("donor_achievements")
            .orderBy("totalPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                var rank = 1
                queryDocumentSnapshots.forEach { doc ->
                    doc.toObject(DonorAchievement::class.java)?.let { achievement ->
                        achievement.rank = rank++
                        doc.reference.set(achievement)
                    }
                }
            }
    }
}
