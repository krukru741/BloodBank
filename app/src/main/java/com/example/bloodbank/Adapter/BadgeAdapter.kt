package com.example.bloodbank.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.R

/**
 * BadgeAdapter - RecyclerView adapter for displaying earned badges.
 * Kotlin version with horizontal layout support.
 */
class BadgeAdapter(private var badges: List<String> = emptyList()) : 
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(badges[position])
    }
    
    override fun getItemCount(): Int = badges.size
    
    /**
     * Update badges list and refresh UI.
     */
    fun updateBadges(newBadges: List<String>) {
        badges = newBadges
        notifyDataSetChanged()
    }
    
    inner class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeIcon: ImageView = itemView.findViewById(R.id.badgeIcon)
        private val badgeNameText: TextView = itemView.findViewById(R.id.badgeNameText)
        
        fun bind(badgeName: String) {
            badgeNameText.text = badgeName
            
            // Fallback to available icons since ic_badge_* icons are missing
            val iconRes = when (badgeName) {
                "First Blood" -> R.drawable.ic_blood_drop
                "Regular Donor" -> R.drawable.ic_blood_drop
                "Streak Master" -> R.drawable.ic_time
                "Emergency Hero" -> R.drawable.ic_emergency
                "Blood Guardian" -> R.drawable.ic_blood
                "Blood Champion" -> R.drawable.ic_blood
                "Blood Legend" -> R.drawable.ic_blood
                else -> R.drawable.ic_blood_drop
            }
            badgeIcon.setImageResource(iconRes)
        }
    }
}
