package com.example.bloodbank.models;

public class Notification {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type; // emergency_request, donation, achievement, etc.
    private String relatedId; // ID of the related entity (requestId, donationId, etc.)
    private long timestamp;
    private boolean read;

    // Default constructor required for Firebase
    public Notification() {
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public Notification(String notificationId, String userId, String title, String message,
                       String type, String relatedId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedId = relatedId;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
} 