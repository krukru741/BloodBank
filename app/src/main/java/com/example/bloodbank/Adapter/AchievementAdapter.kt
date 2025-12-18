package com.example.bloodbank.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.DonorAchievement
import com.example.bloodbank.R

/**
 * AchievementAdapter - RecyclerView adapter for leaderboard.
 * Kotlin version for displaying donor rankings.
 */
class AchievementAdapter(private var achievements: List<DonorAchievement> = emptyList()) : 
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], position + 1)
    }
    
    override fun getItemCount(): Int = achievements.size
    
    /**
     * Update achievements list and refresh UI.
     */
    fun updateAchievements(newAchievements: List<DonorAchievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }
    
    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rankText)
        private val donorNameText: TextView = itemView.findViewById(R.id.donorNameText)
        private val pointsText: TextView = itemView.findViewById(R.id.pointsText)
        private val badgeCountText: TextView = itemView.findViewById(R.id.badgeCountText)
        
        fun bind(achievement: DonorAchievement, position: Int) {
            rankText.text = "#$position"
            donorNameText.text = achievement.donorId // Could be replaced with actual name
            pointsText.text = "${achievement.totalPoints} pts"
            badgeCountText.text = "${achievement.badges.size} badges"
            
            // Highlight top 3
            val backgroundColor = when (position) {
                1 -> android.graphics.Color.parseColor("#FFD700") // Gold
                2 -> android.graphics.Color.parseColor("#C0C0C0") // Silver
                3 -> android.graphics.Color.parseColor("#CD7F32") // Bronze
                else -> android.graphics.Color.TRANSPARENT
            }
            itemView.setBackgroundColor(backgroundColor)
        }
    }
}
