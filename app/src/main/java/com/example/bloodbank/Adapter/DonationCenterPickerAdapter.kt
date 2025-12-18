package com.example.bloodbank.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.DonationCenter
import com.example.bloodbank.R

/**
 * DonationCenterPickerAdapter - RecyclerView adapter for selecting donation centers.
 * Kotlin version with click listener support.
 */
class DonationCenterPickerAdapter(
    private var centers: List<DonationCenter> = emptyList(),
    private val onCenterSelected: (DonationCenter) -> Unit = {}
) : RecyclerView.Adapter<DonationCenterPickerAdapter.CenterViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_center, parent, false)
        return CenterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {
        holder.bind(centers[position])
    }
    
    override fun getItemCount(): Int = centers.size
    
    /**
     * Update centers list and refresh UI.
     */
    fun updateCenters(newCenters: List<DonationCenter>) {
        centers = newCenters
        notifyDataSetChanged()
    }
    
    inner class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val centerNameText: TextView = itemView.findViewById(R.id.centerNameText)
        private val addressText: TextView = itemView.findViewById(R.id.addressText)
        private val hoursText: TextView = itemView.findViewById(R.id.hoursText)
        
        fun bind(center: DonationCenter) {
            centerNameText.text = center.name
            addressText.text = center.address
            hoursText.text = "Hours: ${center.operatingHours}"
            
            itemView.setOnClickListener {
                onCenterSelected(center)
            }
        }
    }
}
