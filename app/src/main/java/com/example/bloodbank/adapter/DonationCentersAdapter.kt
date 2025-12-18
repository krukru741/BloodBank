package com.example.bloodbank.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.DonationCenter
import com.example.bloodbank.R // Assuming R.layout.item_donation_center and TextView IDs are here

class DonationCentersAdapter(private val centersList: MutableList<DonationCenter>) :
    RecyclerView.Adapter<DonationCentersAdapter.CenterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_center, parent, false)
        return CenterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {
        val center = centersList[position]
        holder.bind(center)
    }

    override fun getItemCount(): Int = centersList.size

    inner class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val addressTextView: TextView = itemView.findViewById(R.id.textViewAddress)
        private val phoneTextView: TextView = itemView.findViewById(R.id.textViewPhone)
        private val emailTextView: TextView = itemView.findViewById(R.id.textViewEmail)
        private val cityTextView: TextView = itemView.findViewById(R.id.textViewCity)

        fun bind(center: DonationCenter) {
            nameTextView.text = center.name
            addressTextView.text = center.address
            phoneTextView.text = center.phone
            emailTextView.text = center.email
            cityTextView.text = center.city // Assuming 'city' is now a property of DonationCenter.kt
        }
    }
}