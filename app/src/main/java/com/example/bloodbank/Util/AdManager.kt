package com.example.bloodbank.Util

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * AdManager - Handles ad loading and error management.
 * Kotlin singleton with lazy initialization.
 */
class AdManager private constructor(context: Context) {
    
    private val appContext = context.applicationContext
    
    companion object {
        private const val TAG = "AdManager"
        
        @Volatile
        private var instance: AdManager? = null
        
        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context).also { instance = it }
            }
        }
    }
    
    /**
     * Handle ad loading errors with specific error codes.
     */
    fun handleAdError(errorCode: Int, errorMessage: String) {
        Log.e(TAG, "Ad Error: $errorCode - $errorMessage")
        
        when (errorCode) {
            11020 -> { // Forward failed error
                Log.w(TAG, "Ad forward failed, retrying...")
                retryAdRequest()
            }
            10000 -> { // General error
                Log.w(TAG, "General ad error occurred")
            }
            else -> {
                Log.e(TAG, "Unknown ad error: $errorCode")
            }
        }
    }
    
    /**
     * Retry ad request with backoff strategy.
     */
    private fun retryAdRequest() {
        // Implement retry logic here:
        // 1. Wait for network connectivity
        // 2. Implement exponential backoff
        // 3. Fall back to cached ads
    }
    
    /**
     * Show user-friendly error toast.
     */
    fun showAdErrorToast() {
        Toast.makeText(appContext, "Ad loading failed. Please try again later.", Toast.LENGTH_SHORT).show()
    }
}
