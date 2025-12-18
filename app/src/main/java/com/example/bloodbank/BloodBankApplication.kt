package com.example.bloodbank

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * BloodBankApplication - Application class for Hilt dependency injection.
 * Required for Hilt to work properly.
 */
@HiltAndroidApp
class BloodBankApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
