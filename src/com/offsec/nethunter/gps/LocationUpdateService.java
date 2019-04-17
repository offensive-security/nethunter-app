package com.offsec.nethunter.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class LocationUpdateService extends Service implements GpsdServer.ConnectionListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private KaliGPSUpdates.Receiver updateReceiver;
    private static final String TAG = "LocationUpdateService";
    private GoogleApiClient apiClient = null;
    private boolean requestedLocationUpdates = false;
    private Socket clientSocket = null;

    private final IBinder binder = new ServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * Formats the number of satellites from the #Location into a
     * string.  In case #LocationManager.NETWORK_PROVIDER is used, it
     * returns the faked value "1", because some software refuses to
     * work with a "0" or an empty value.
     */
    public String formatSatellites(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            Bundle bundle = location.getExtras();
            return bundle != null
                    ? "" + bundle.getInt("satellites")
                    : "";
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
            // fake this variable
            return "1";
        else
            return "";
    }

    /**
     * Formats the altitude from the #Location into a string, with a
     * second unit field ("M" for meters).  If the altitude is
     * unknown, it returns two empty fields.
     */
    public String formatAltitude(Location location) {
        String s = "";
        if (location.hasAltitude())
            s += location.getAltitude() + ",M";
        else
            s += ",";
        return s;
    }

    /**
     * Calculates the NMEA checksum of the specified string.  Pass the
     * portion of the line between '$' and '*' here.
     */
    private String checksum(String s) {
        int checksum = 0;

        for (int i = 0; i < s.length(); i++)
            checksum = checksum ^ s.charAt(i);

        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1)
            hex = "0" + hex;
        return ("*" + hex.toUpperCase());
    }

    /**
     * Formats the time from the #Location into a string.
     */
    @SuppressLint("DefaultLocale")
    public static String formatTime(Location location) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HHmmss");
        return dtf.print(new DateTime(location.getTime()));
    }

    /**
     * Formats the surface position (latitude and longitude) from the
     * #Location into a string.
     */
    public static String formatPosition(Location location) {
        double latitude = location.getLatitude();
        char nsSuffix = latitude < 0 ? 'S' : 'N';
        latitude = Math.abs(latitude);

        double longitude = location.getLongitude();
        char ewSuffix = longitude < 0 ? 'W' : 'E';
        longitude = Math.abs(longitude);
        @SuppressLint("DefaultLocale") String lat = String.format("%02d%02d.%04d,%c",
                (int) latitude,
                (int) (latitude * 60) % 60,
                (int) (latitude * 60 * 10000) % 10000,
                nsSuffix);
        @SuppressLint("DefaultLocale") String lon = String.format("%03d%02d.%04d,%c",
                (int) longitude,
                (int) (longitude * 60) % 60,
                (int) (longitude * 60 * 10000) % 10000,
                ewSuffix);
        return lat + "," + lon;
    }

    @Override
    public void onSocketConnected(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (requestedLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google API Client connection failed");
    }


    public class ServiceBinder extends Binder {
        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void requestUpdates(KaliGPSUpdates.Receiver receiver) {
        requestedLocationUpdates = true;
        this.updateReceiver = receiver;

        GpsdServer gpsdServer = null;
        gpsdServer = new GpsdServer(this);
        if (gpsdServer != null) {
            gpsdServer.execute(null, null);
            Log.d(TAG, "GPSDServer Async Task Begun");
        } else {
            Log.d(TAG, "Error starting gpsd server");
        }


        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(LocationUpdateService.this, this, this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!apiClient.isConnected()) {
            apiClient.connect();
        } else {
            startLocationUpdates();
        }
    }

    public void stopUpdates() {
        Log.d(TAG, "In stopUpdates");
        requestedLocationUpdates = false;
        this.updateReceiver = null;
        stopSelf();
    }

    private void startLocationUpdates() {
        Log.d(TAG, "in startLocationUpdates");
        final LocationRequest lr = LocationRequest.create()
                .setExpirationDuration(1000 * 3600 * 2) /*2 hrs*/
                .setFastestInterval(100L)
                .setInterval(1000L / 2L) /*2 hz updates*/
                .setMaxWaitTime(600L)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        Log.d(TAG, "Requesting permissions marshmallow");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, lr, locationListener);
        }
    }

    private boolean firstupdate = true;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String nmeaSentence = nmeaSentenceFromLocation(location);

            // Workaround to allow network operations in main thread
            if (android.os.Build.VERSION.SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            if (clientSocket != null) {

                PrintWriter out = null;
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "NMEA update: "+nmeaSentence);
                out.println(nmeaSentence);

                if (updateReceiver != null) {
                    if (firstupdate) {
                        firstupdate = false;
                        updateReceiver.onFirstPositionUpdate();
                    }
                    updateReceiver.onPositionUpdate(nmeaSentence);
                }
            }
        }
    };


    private String nmeaSentenceFromLocation(Location location) {

//       from: https://github.com/ya-isakov/blue-nmea-mirror/blob/master/src/Source.java
        String time = formatTime(location);
        String position = formatPosition(location);

        String innerSentence = "GPGGA," + time + "," +
                position + ",1," +
                formatSatellites(location) + "," +
                location.getAccuracy() + "," +
                formatAltitude(location) + ",,,,";

//        Adds checksum and initial $
        String checksum = checksum(innerSentence);
        return "$" + innerSentence + checksum;

    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, locationListener);
        }
        super.onDestroy();
    }
}

