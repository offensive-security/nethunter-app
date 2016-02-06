package com.offsec.nethunter.GPS;


/**
 * Created by Danial on 2/23/2015.
 * https://github.com/danialgoodwin/android-app-samples/blob/master/gps-satellite-nmea-info/app/src/main/java/net/simplyadvanced/gpsandsatelliteinfo/GpsPosition.java
 */

public class GpsPosition {

    public float time = 0.0f;
    public float latitude = 0.0f;
    public float longitude = 0.0f;
    public boolean isFixed = false;
    public int quality = 0;
    public float direction = 0.0f;
    public float altitude = 0.0f;
    public float velocity = 0.0f;

    public void updateIsfixed() {
        isFixed = quality > 0;
    }

    @Override
    public String toString() {
        return String.format("GpsPosition: latitude: %f, longitude: %f, time: %f, quality: %d, " +
                        "direction: %f, altitude: %f, velocity: %f", latitude, longitude, time, quality,
                direction, altitude, velocity);
    }

}