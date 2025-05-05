package com.example.bloodbank.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodbank.Model.Message;
import com.example.bloodbank.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageText.setText(message.getMessage());
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.timeText.setText(time);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView timeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
} 