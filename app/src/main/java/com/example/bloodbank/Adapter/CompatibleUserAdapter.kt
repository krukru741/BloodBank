package com.example.bloodbank.Adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.CompatibleUser
import com.example.bloodbank.R
import com.google.android.material.button.MaterialButton

/**
 * CompatibleUserAdapter - RecyclerView adapter for compatible donors.
 * Kotlin version with call functionality.
 */
class CompatibleUserAdapter(private var users: List<CompatibleUser> = emptyList()) : 
    RecyclerView.Adapter<CompatibleUserAdapter.CompatibleUserViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompatibleUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return CompatibleUserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CompatibleUserViewHolder, position: Int) {
        holder.bind(users[position])
    }
    
    override fun getItemCount(): Int = users.size
    
    /**
     * Update compatible users list and refresh UI.
     */
    fun updateUsers(newUsers: List<CompatibleUser>) {
        users = newUsers
        notifyDataSetChanged()
    }
    
    inner class CompatibleUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val bloodGroupText: TextView = itemView.findViewById(R.id.bloodGroupText)
        private val addressText: TextView = itemView.findViewById(R.id.addressText)
        private val callButton: MaterialButton = itemView.findViewById(R.id.callButton)
        
        fun bind(user: CompatibleUser) {
            nameText.text = user.name
            bloodGroupText.text = "Blood Group: ${user.bloodGroup}"
            addressText.text = user.address
            
            callButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${user.phoneNumber}")
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}
