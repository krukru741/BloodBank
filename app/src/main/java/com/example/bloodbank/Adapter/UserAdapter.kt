package com.example.bloodbank.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.User
import com.example.bloodbank.R
import com.example.bloodbank.UserProfileActivity
import de.hdodenhof.circleimageview.CircleImageView

/**
 * UserAdapter - RecyclerView adapter for displaying user list.
 * Kotlin version with concise syntax and update method.
 */
class UserAdapter(private var users: List<User> = emptyList()) : 
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }
    
    override fun getItemCount(): Int = users.size
    
    /**
     * Update users list and refresh UI.
     */
    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
    
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val bloodGroupText: TextView = itemView.findViewById(R.id.bloodGroupText)
        private val addressText: TextView = itemView.findViewById(R.id.addressText)
        
        fun bind(user: User) {
            nameText.text = user.name
            bloodGroupText.text = user.bloodGroup
            addressText.text = user.address
            
            // Load profile image if available
            // Glide or other image loading can be added here
            
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, UserProfileActivity::class.java).apply {
                    putExtra("userId", user.id)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}
