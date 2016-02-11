package com.offsec.nethunter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.GPS.GpsPosition;
import com.offsec.nethunter.GPS.NmeaUtils;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class KaliGpsServiceFragment extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    private static final String TAG = "KaliGpsServiceFragment";
    private TextView GpsTextview;

    static NhPaths nh;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public KaliGpsServiceFragment() {
    }

    public static KaliGpsServiceFragment newInstance(int sectionNumber) {
        KaliGpsServiceFragment fragment = new KaliGpsServiceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gps, container, false);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();
        GpsTextview = (TextView) rootView.findViewById(R.id.GpsTextview);

        nh = new NhPaths();

        addClickListener(R.id.start_kismet, new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        ShellExecuter exe = new ShellExecuter();
                        String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/start-gpsd'";
                        Log.d(TAG, command);
                        exe.RunAsRootOutput(command);
                    }
                }).start();
                intentClickListener_NH("/usr/bin/start-kismet"); // start kismet
                nh.showMessage("Starting Kismet with GPS support");
                setupGpsListener();
            }
        }, rootView);

        addClickListener(R.id.gps_stop, new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        ShellExecuter exe = new ShellExecuter();
                        String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/stop-gpsd'";
                        Log.d(TAG, command);
                        exe.RunAsRootOutput(command);
                    }
                }).start();
                nh.showMessage("Stopping GPS Server");
                //stopGpsListener();
            }
        }, rootView);

        return rootView;
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

    private void setupGpsListener() {

        Log.d(TAG, "setupGpsListener");

        Log.d(TAG, "Start LocationManager");
        final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Log.d(TAG, "Requesting permissions marshmallow");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //Log.d(TAG, "onLocationChanged()");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    //Log.d(TAG, "onStatusChanged(provider=" + provider + ", status=" + status + ")");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    //Log.d(TAG, "onProviderEnabled(provider=" + provider + ")");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    //Log.d(TAG, "onProviderDisabled(provider=" + provider + ")");
                }
            });

            lm.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    // This is where we log NMEA to file
                    writeToFile(nmea);

                    GpsTextview.setText(nmea + "\n");

                    // SHOW EVERYTHING
                    //GpsTextview.setText(GpsTextview.getText() + "\n" + nmea + "\n" + extractNmeaInfo(timestamp, nmea));
                }
            });
        }
        }

    private void writeToFile(String data) {
        try {
            String filename = nh.CHROOT_PATH + "/tmp/gps" ;

            FileWriter fstream = new FileWriter(filename, true);
            fstream.write(data + "\n");
            fstream.flush();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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

    private void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
        }
    }

}