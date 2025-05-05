package com.example.bloodbank.Model;

public class Notification {
    private String userId;
    private String message;
    private String time;
    private boolean isRead;

    public Notification() {
        // Default constructor required for Firebase
    }

    public Notification(String userId, String message, String time, boolean isRead) {
        this.userId = userId;
        this.message = message;
        this.time = time;
        this.isRead = isRead;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
