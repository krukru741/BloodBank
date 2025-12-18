package com.example.bloodbank.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.EmergencyRequestDetailsActivity
import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.R
import com.google.android.material.card.MaterialCardView

/**
 * EmergencyRequestAdapter - RecyclerView adapter for emergency requests.
 * Kotlin version with priority-based styling.
 */
class EmergencyRequestAdapter(private var requests: List<EmergencyRequest> = emptyList()) : 
    RecyclerView.Adapter<EmergencyRequestAdapter.EmergencyRequestViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_request, parent, false)
        return EmergencyRequestViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: EmergencyRequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }
    
    override fun getItemCount(): Int = requests.size
    
    /**
     * Update emergency requests list and refresh UI.
     */
    fun updateRequests(newRequests: List<EmergencyRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
    
    inner class EmergencyRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val patientNameText: TextView = itemView.findViewById(R.id.patientNameText)
        private val bloodGroupText: TextView = itemView.findViewById(R.id.bloodGroupText)
        private val hospitalText: TextView = itemView.findViewById(R.id.hospitalText)
        private val urgencyText: TextView = itemView.findViewById(R.id.urgencyText)
        
        fun bind(request: EmergencyRequest) {
            patientNameText.text = request.patientName
            bloodGroupText.text = request.bloodGroup
            hospitalText.text = request.hospital
            urgencyText.text = "Urgency: ${request.urgencyLevel}"
            
            // Set card color based on urgency
            val cardColor = when (request.urgencyLevel) {
                "Critical" -> android.graphics.Color.parseColor("#FFEBEE")
                "Urgent" -> android.graphics.Color.parseColor("#FFF3E0")
                else -> android.graphics.Color.parseColor("#E8F5E9")
            }
            cardView.setCardBackgroundColor(cardColor)
            
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, EmergencyRequestDetailsActivity::class.java).apply {
                    putExtra("requestId", request.requestId)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}
