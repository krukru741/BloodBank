package com.example.bloodbank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonationCentersAdapter extends RecyclerView.Adapter<DonationCentersAdapter.CenterViewHolder> {
    private final List<DonationCenter> centersList;

    public DonationCentersAdapter(List<DonationCenter> centersList) {
        this.centersList = centersList;
    }

    @NonNull
    @Override
    public CenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_center, parent, false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CenterViewHolder holder, int position) {
        DonationCenter center = centersList.get(position);
        holder.nameTextView.setText(center.getName());
        holder.addressTextView.setText(center.getAddress());
        holder.phoneTextView.setText(center.getPhone());
        holder.emailTextView.setText(center.getEmail());
        holder.cityTextView.setText(center.getCity());
    }

    @Override
    public int getItemCount() {
        return centersList.size();
    }

    static class CenterViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;
        TextView phoneTextView;
        TextView emailTextView;
        TextView cityTextView;

        CenterViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            addressTextView = itemView.findViewById(R.id.textViewAddress);
            phoneTextView = itemView.findViewById(R.id.textViewPhone);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            cityTextView = itemView.findViewById(R.id.textViewCity);
        }
    }
} 