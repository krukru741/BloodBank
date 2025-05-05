package com.example.bloodbank.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

public class GooglePlayServicesUtils {
    private static final String TAG = "GooglePlayServicesUtils";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    /**
     * Check if Google Play Services is available and up to date
     * @param context The application context
     * @return true if Google Play Services is available and up to date, false otherwise
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.w(TAG, "Google Play Services is not available but can be fixed");
                return false;
            } else {
                Log.e(TAG, "This device is not supported by Google Play Services");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Show dialog to update Google Play Services if needed
     * @param activity The activity to show the dialog from
     * @return true if the dialog was shown, false if Google Play Services is not available
     */
    public static boolean showGooglePlayServicesUpdateDialog(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if location settings are enabled
     * @param activity The activity to check settings from
     * @param onResult Callback for the result
     */
    public static void checkLocationSettings(Activity activity, LocationSettingsCallback onResult) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(activity)
                .checkLocationSettings(builder.build());

        task.addOnCompleteListener(activity, task1 -> {
            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                onResult.onSuccess(response);
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            onResult.onError("Could not start location settings resolution");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        onResult.onError("Location settings are not available");
                        break;
                    default:
                        onResult.onError("An error occurred checking location settings");
                        break;
                }
            }
        });
    }

    /**
     * Interface for location settings callback
     */
    public interface LocationSettingsCallback {
        void onSuccess(LocationSettingsResponse response);
        void onError(String message);
    }
} 