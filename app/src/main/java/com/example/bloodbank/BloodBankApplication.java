package com.example.bloodbank;

import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import dagger.hilt.android.HiltAndroidApp; // Added import for HiltAndroidApp

@HiltAndroidApp // Added HiltAndroidApp annotation
public class BloodBankApplication extends MultiDexApplication {
    private static final String TAG = "BloodBankApplication";

    @Override
    public void onCreate() {
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Check Google Play Services availability
            checkGooglePlayServices();
            
            super.onCreate();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing app: " + e.getMessage());
            super.onCreate();
        }
    }
    
    /**
     * Check if Google Play Services is available
     */
    private void checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services is not available (status: " + resultCode + ")");
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.i(TAG, "Google Play Services error is resolvable");
            } else {
                Log.e(TAG, "This device is not supported by Google Play Services");
            }
        } else {
            Log.i(TAG, "Google Play Services is available");
        }
    }
}