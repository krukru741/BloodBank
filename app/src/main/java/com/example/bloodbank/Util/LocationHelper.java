package com.example.bloodbank.Util;

import android.location.Location;

public class LocationHelper {
    private static final double MAX_DISTANCE_KM = 50.0; // Maximum distance to notify donors (50km)

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        // Get distance in meters and convert to kilometers
        return location1.distanceTo(location2) / 1000.0;
    }

    public static boolean isWithinRange(double lat1, double lon1, double lat2, double lon2) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= MAX_DISTANCE_KM;
    }
} 