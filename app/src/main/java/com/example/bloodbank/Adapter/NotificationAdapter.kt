package com.example.bloodbank.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodbank.Model.Notification
import com.example.bloodbank.Model.User
import com.example.bloodbank.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
class NotificationAdapter(
    private val context: Context,
    private var notificationList: MutableList<Notification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.bind(notification)
    }
    
    override fun getItemCount(): Int = notificationList.size
    
    /**
     * Update the notification list and notify adapter.
     */
    fun updateNotifications(newNotifications: List<Notification>) {
        notificationList.clear()
        notificationList.addAll(newNotifications)
        notifyDataSetChanged()
    }
    
    /**
     * ViewHolder for notification items.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.notification_profile_image)
        private val username: TextView = itemView.findViewById(R.id.notification_name)
        private val message: TextView = itemView.findViewById(R.id.notification_text)
        private val time: TextView = itemView.findViewById(R.id.notification_date)
        
        fun bind(notification: Notification) {
            // Set message and time
            message.text = notification.message
            time.text = notification.time
            
            // Get user details
            val userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(notification.userId)
            
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            username.text = it.name
                            
                            // Load profile image
                            val profileImagePath = it.profileImagePath
                            if (!profileImagePath.isNullOrEmpty()) {
                                Glide.with(context)
                                    .load(File(profileImagePath))
                                    .placeholder(R.drawable.profile_pic)
                                    .error(R.drawable.profile_pic)
                                    .into(profileImage)
                            } else {
                                profileImage.setImageResource(R.drawable.profile_pic)
                            }
                        }
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    // Handle error silently
                }
            })
        }
    }
}
