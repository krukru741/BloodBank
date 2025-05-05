package com.example.bloodbank.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AdManager {
    private static final String TAG = "AdManager";
    private static AdManager instance;
    private final Context context;

    private AdManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context);
        }
        return instance;
    }

    public void handleAdError(int errorCode, String errorMessage) {
        Log.e(TAG, "Ad Error: " + errorCode + " - " + errorMessage);
        
        // Handle specific error codes
        switch (errorCode) {
            case 11020: // Forward failed error
                Log.w(TAG, "Ad forward failed, retrying...");
                retryAdRequest();
                break;
            case 10000: // General error
                Log.w(TAG, "General ad error occurred");
                break;
            default:
                Log.e(TAG, "Unknown ad error: " + errorCode);
                break;
        }
    }

    private void retryAdRequest() {
        // Implement retry logic here
        // This could include:
        // 1. Waiting for network connectivity
        // 2. Implementing exponential backoff
        // 3. Falling back to cached ads
    }

    public void showAdErrorToast() {
        Toast.makeText(context, "Ad loading failed. Please try again later.", Toast.LENGTH_SHORT).show();
    }
} 