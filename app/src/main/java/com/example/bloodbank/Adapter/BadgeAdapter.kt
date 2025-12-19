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
            
            // Use Android's built-in star icon for all badges
            // TODO: Add custom badge icons to res/drawable
            badgeIcon.setImageResource(android.R.drawable.btn_star_big_on)
        }
    }
}
