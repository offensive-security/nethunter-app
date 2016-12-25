package com.offsec.nethunter.GPS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import java.net.ServerSocket;
import java.net.Socket;

public class LocationUpdateService extends Service implements GpsdServer.ConnectionListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int GGA_LENGTH_MAX = 90;
    private KaliGPSUpdates.Receiver updateReceiver;
    private static final String TAG = "LocationUpdateService";
    private GoogleApiClient apiClient = null;
    private boolean requestedLocationUpdates = false;
    private Socket clientSocket = null;
    private ServerSocket serverSocket = null;

    public LocationUpdateService() {
    }

    private final IBinder binder = new ServiceBinder();

    /**
     * Calculates the NMEA checksum of the specified string.  Pass the
     * portion of the line between '$' and '*' here.
     */
    private String checksum(String s) {
        byte[] bytes = s.getBytes();
        int checksum = 0;

        for (int i = bytes.length - 1; i >= 0; --i)
            checksum = checksum ^ bytes[i];

        return "*" + String.valueOf(checksum);
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
     * Formats the date from the #Location into a string.
     */
    public static String formatDate(Location location) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MMddyy");
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
        @SuppressLint("DefaultLocale")

// Todo: format strings in a reasonable way
        String lat = String.format("%02d%02d.%04d,%c",
                (int) latitude,
                (int) (latitude * 60) % 60,
                (int) (latitude * 60 * 10000) % 10000,
                nsSuffix);
        String lon = String.format("%03d%02d.%04d,%c",
                (int) longitude,
                (int) (longitude * 60) % 60,
                (int) (longitude * 60 * 10000) % 10000,
                ewSuffix);
        return lat + "," + lon;
    }

    @Override
    public void onSocketConnected(Socket clientSocket, ServerSocket serverSocket) {
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
        try {
            gpsdServer = new GpsdServer(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gpsdServer.execute(null, null);
        Log.d(TAG, "GPSDServer Async Task Begun");


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
//        3) send GPS updates as they occur
    }

    public void setUpdateReceiver(KaliGPSUpdates.Receiver updateReceiver) {
        this.updateReceiver = updateReceiver;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "in startLocationUpdates");
        final LocationRequest lr = LocationRequest.create()
                .setExpirationDuration(1000 * 3600 * 2) /*2 hrs*/
                .setInterval(1000 / 2L) /*2 hz updates*/
                .setMaxWaitTime(1000L)
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
//            CharsetEncoder enc = Charset.forName("US-ASCII").newEncoder();



            if (clientSocket != null) {

                PrintWriter out = null;
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.println(nmeaSentence);



//                ByteBuffer buf = ByteBuffer.allocate(GGA_LENGTH_MAX);
//                buf.clear();
//                buf.put(nmeaSentence.getBytes());
//                buf.flip();
//
//                while (buf.hasRemaining()) {
//                    try {
//                        clientSocket.getChannel().write(buf);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

//                try {
//                    clientSocket.getChannel().write(enc.encode(CharBuffer.wrap(nmeaSentence)));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

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

//            from: https://github.com/ya-isakov/blue-nmea-mirror/blob/master/src/Source.java
        String time = formatTime(location);
        String date = formatDate(location);
        String position = formatPosition(location);

        String gpggaSentence = "GPGGA," + time + "," +
                position + ",1," +
                NMEA.formatSatellites(location) + "," +
                location.getAccuracy() + "," +
                NMEA.formatAltitude(location) + ",,,,";

//        Adds checksum and initial $
        String checksum = checksum(gpggaSentence);
        String fullSentence = "$" + gpggaSentence + checksum;
        return fullSentence;

//        sendWithChecksum("GPGLL," + position + "," + time + ",A");
//        sendWithChecksum("GPRMC," + time + ",A," +
//                position + "," +
//                NMEA.formatSpeedKt(location) + "," +
//                NMEA.formatBearing(location) + "," +
//                date + ",,");
//


    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
    }
}