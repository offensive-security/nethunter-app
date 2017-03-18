package com.offsec.nethunter.gps;


import java.util.Locale;

/**
 * Created by Danial on 2/23/2015.
 * https://github.com/danialgoodwin/android-app-samples/blob/master/gps-satellite-nmea-info/app/src/main/java/net/simplyadvanced/gpsandsatelliteinfo/GpsPosition.java
 */

public class GpsPosition {

    public float time = 0.0f;
    private float latitude = 0.0f;
    private float longitude = 0.0f;
    private int quality = 0;
    private float direction = 0.0f;
    private float altitude = 0.0f;
    private float velocity = 0.0f;

    public void updateIsfixed() {
        boolean isFixed = quality > 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "GpsPosition: latitude: %f, longitude: %f, time: %f, quality: %d, " +
                        "direction: %f, altitude: %f, velocity: %f", latitude, longitude, time, quality,
                direction, altitude, velocity);
    }

}