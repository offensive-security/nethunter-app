package com.offsec.nethunter;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.offsec.nethunter.GPS.GpsPosition;
import com.offsec.nethunter.GPS.NmeaUtils;
import com.offsec.nethunter.utils.NhPaths;

import java.io.FileWriter;
import java.io.IOException;


public class KaliGpsServiceFragment extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    private static final String TAG = "KaliGpsServiceFragment";

    static NhPaths nh;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public KaliGpsServiceFragment() {
    }

    public static PineappleFragment newInstance(int sectionNumber) {
        PineappleFragment fragment = new PineappleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.pineapple, container, false);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();

        return rootView;
    }


    private void setupGpsListener(Context context) {

        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "onLocationChanged()");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d(TAG, "onStatusChanged(provider=" + provider + ", status=" + status + ")");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d(TAG, "onProviderEnabled(provider=" + provider + ")");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d( TAG, "onProviderDisabled(provider=" + provider + ")");
                }
            });

            lm.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    Log.d(TAG, "onNmeaReceived()");
                    // This is where we log NMEA to file
                    FileWriter f;
                    try {
                        f = new FileWriter("/tmp/gps", true);
                        f.write(nmea + "\n" + extractNmeaInfo(timestamp, nmea));
                        f.flush();
                        f.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static String extractGpsStatusInfo(GpsStatus gpsStatus) {
        String info = "GpsStatus: ";
        if (gpsStatus == null) { return info + "null"; }

        info += "\nMax satellites: " + gpsStatus.getMaxSatellites();
        info += "\nTime to first fix: " + gpsStatus.getTimeToFirstFix();

        int numSattelites = 0;
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        for (GpsSatellite satellite : satellites) {
            info += "\nSatellite " + satellite.toString() + ": azimuth=" + satellite.getAzimuth();
            info += "\nSatellite " + satellite.toString() + ": elevation=" + satellite.getElevation();
            info += "\nSatellite " + satellite.toString() + ": prn=" + satellite.getPrn();
            info += "\nSatellite " + satellite.toString() + ": snr=" + satellite.getSnr();
            info += "\nSatellite " + satellite.toString() + ": hasAlmanac=" + satellite.hasAlmanac();
            info += "\nSatellite " + satellite.toString() + ": usedInFix=" + satellite.usedInFix();
            info += "\nSatellite " + satellite.toString() + ": hasEphemeris=" + satellite.hasEphemeris();
            numSattelites++;
        }
        info += "\nNumber of satellites: " + numSattelites;

        return info;
    }

    private static String extractNmeaInfo(long timestamp, String nmea) {
        String info = "NmeaInfo: ";
        if (nmea == null) { return info + "null"; }
        if (nmea.isEmpty()) { return info + "empty"; }

        info += "\ntimestamp: " + timestamp;

        Log.d(TAG, "extractNmeaInfo(), nmea=" + nmea);

        GpsPosition gpsPosition = new GpsPosition();
        boolean isSuccessfulParse = NmeaUtils.parse(nmea, gpsPosition);
        if (isSuccessfulParse) {
            info += "\nGPS fix data: " + NmeaUtils.getType(nmea);
            info += "\n" + gpsPosition.toString();
        } else {
            info += "unsuccessful parse";
        }

        return info;
    }

}