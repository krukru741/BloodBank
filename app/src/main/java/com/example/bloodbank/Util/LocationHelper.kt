package com.example.bloodbank.Util

import android.location.Location

/**
 * LocationHelper - Utility for calculating distances between coordinates.
 * Kotlin object for singleton pattern.
 */
object LocationHelper {
    private const val MAX_DISTANCE_KM = 50.0 // Maximum distance to notify donors (50km)
    
    /**
     * Calculate distance between two coordinates in kilometers.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val location1 = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        
        val location2 = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        
        // Get distance in meters and convert to kilometers
        return location1.distanceTo(location2) / 1000.0
    }
    
    /**
     * Check if two coordinates are within the maximum range.
     */
    fun isWithinRange(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {
        return calculateDistance(lat1, lon1, lat2, lon2) <= MAX_DISTANCE_KM
    }
}
