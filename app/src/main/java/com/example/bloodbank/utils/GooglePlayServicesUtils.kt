package com.example.bloodbank.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes

/**
 * GooglePlayServicesUtils - Utilities for Google Play Services and location settings.
 * Kotlin object for singleton pattern.
 */
object GooglePlayServicesUtils {
    private const val TAG = "GooglePlayServicesUtils"
    private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    const val REQUEST_CHECK_SETTINGS = 1001
    
    /**
     * Check if Google Play Services is available and up to date.
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        
        return when {
            resultCode == ConnectionResult.SUCCESS -> true
            apiAvailability.isUserResolvableError(resultCode) -> {
                Log.w(TAG, "Google Play Services is not available but can be fixed")
                false
            }
            else -> {
                Log.e(TAG, "This device is not supported by Google Play Services")
                false
            }
        }
    }
    
    /**
     * Show dialog to update Google Play Services if needed.
     * @return true if the dialog was shown, false if Google Play Services is available
     */
    fun showGooglePlayServicesUpdateDialog(activity: Activity): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        
        return if (resultCode != ConnectionResult.SUCCESS && apiAvailability.isUserResolvableError(resultCode)) {
            apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
            true
        } else {
            false
        }
    }
    
    /**
     * Check if location settings are enabled.
     */
    fun checkLocationSettings(activity: Activity, onResult: LocationSettingsCallback) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        
        val task = LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())
        
        task.addOnCompleteListener(activity) { task1 ->
            try {
                val response = task1.getResult(ApiException::class.java)
                onResult.onSuccess(response)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            onResult.onError("Could not start location settings resolution")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        onResult.onError("Location settings are not available")
                    }
                    else -> {
                        onResult.onError("An error occurred checking location settings")
                    }
                }
            }
        }
    }
    
    /**
     * Callback interface for location settings check.
     */
    interface LocationSettingsCallback {
        fun onSuccess(response: LocationSettingsResponse)
        fun onError(message: String)
    }
}
