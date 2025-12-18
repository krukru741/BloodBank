package com.example.bloodbank.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.Message
import com.example.bloodbank.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying chat messages in a RecyclerView.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class MessageAdapter(
    private val context: Context,
    private var messageList: MutableList<Message>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == VIEW_TYPE_MY_MESSAGE) {
            LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false)
        }
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }
    
    override fun getItemCount(): Int = messageList.size
    
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }
    
    /**
     * Update the message list and notify adapter.
     */
    fun updateMessages(newMessages: List<Message>) {
        messageList.clear()
        messageList.addAll(newMessages)
        notifyDataSetChanged()
    }
    
    /**
     * ViewHolder for message items.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        
        fun bind(message: Message) {
            messageText.text = message.message
            
            // Format timestamp
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = sdf.format(Date(message.timestamp))
            timeText.text = time
        }
    }
}
