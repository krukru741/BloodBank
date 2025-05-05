package com.example.bloodbank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonationCenterPickerAdapter extends RecyclerView.Adapter<DonationCenterPickerAdapter.CenterViewHolder> {
    private final List<DonationCenter> centersList;
    private final OnCenterSelectedListener listener;

    public interface OnCenterSelectedListener {
        void onCenterSelected(DonationCenter center);
    }

    public DonationCenterPickerAdapter(List<DonationCenter> centersList, OnCenterSelectedListener listener) {
        this.centersList = centersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_center_picker, parent, false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CenterViewHolder holder, int position) {
        DonationCenter center = centersList.get(position);
        holder.nameTextView.setText(center.getName());
        holder.addressTextView.setText(center.getAddress());
        holder.cityTextView.setText(center.getCity());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCenterSelected(center);
            }
        });
    }

    @Override
    public int getItemCount() {
        return centersList.size();
    }

    static class CenterViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;
        TextView cityTextView;

        CenterViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            addressTextView = itemView.findViewById(R.id.textViewAddress);
            cityTextView = itemView.findViewById(R.id.textViewCity);
        }
    }
} 