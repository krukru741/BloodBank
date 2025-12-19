package com.example.bloodbank.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.DonationAppointment
import com.example.bloodbank.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * AppointmentsAdapter - RecyclerView adapter for donation appointments.
 * Kotlin version with date formatting.
 */
class AppointmentsAdapter(private var appointments: List<DonationAppointment> = emptyList()) : 
    RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }
    
    override fun getItemCount(): Int = appointments.size
    
    /**
     * Update appointments list and refresh UI.
     */
    fun updateAppointments(newAppointments: List<DonationAppointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
    
    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val centerText: TextView = itemView.findViewById(R.id.textViewCenter)
        private val dateTimeText: TextView = itemView.findViewById(R.id.textViewDateTime)
        private val statusText: TextView = itemView.findViewById(R.id.textViewStatus)
        
        fun bind(appointment: DonationAppointment) {
            centerText.text = appointment.centerName
            // Combine date and time slot into one field
            val dateStr = dateFormat.format(Date(appointment.appointmentDate))
            dateTimeText.text = "$dateStr - ${appointment.timeSlot}"
            statusText.text = appointment.status
            
            // Set status color
            val statusColor = when (appointment.status) {
                "SCHEDULED" -> android.graphics.Color.parseColor("#4CAF50")
                "COMPLETED" -> android.graphics.Color.parseColor("#2196F3")
                "CANCELLED" -> android.graphics.Color.parseColor("#F44336")
                else -> android.graphics.Color.GRAY
            }
            statusText.setTextColor(statusColor)
        }
    }
}
