package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.GPS.KaliGPSUpdates;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;


public class KaliGpsServiceFragment extends Fragment implements KaliGPSUpdates.Receiver {

    private static final String TAG = "KaliGpsServiceFragment";
    private TextView GpsTextview;

    private static NhPaths nh;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private KaliGPSUpdates.Provider gpsProvider = null;

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
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();
        GpsTextview = (TextView) rootView.findViewById(R.id.GpsTextview);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        nh = new NhPaths();

        addClickListener(R.id.start_kismet, new View.OnClickListener() {
            public void onClick(View v) {

                if (gpsProvider != null) {
                    gpsProvider.onLocationUpdatesRequested(KaliGpsServiceFragment.this);
                }


//
//                intentClickListener_NH(); // start kismet
//                nh.showMessage("Starting Kismet with GPS support");
//
//                // Start TCP Server on port 9000
//                Thread thread = new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                        try {
//                            new Server();
//                            Server.main();
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                        }
//                    }
//                });
//                thread.start();
//
//                // Setup NMEA
//                setupGpsListener();
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
                Server.Shutdown();
                //stopGpsListener();
            }
        }, rootView);

        return rootView;
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }



    @Override
    public void onAttach(Context context) {
        if (context instanceof KaliGPSUpdates.Provider) {
            this.gpsProvider = (KaliGPSUpdates.Provider) context;
        }

        super.onAttach(context);
    }

    @Override
    public void onPositionUpdate(String nmeaSentences) {

    }

    @Override
    public void onServerReady() {

    }

    @Override
    public void onFirstPositionUpdate() {
//        Got first position update, start Kismet
        startKismet();
    }

    private void startKismet() {
            try {
                Intent intent =
                        new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                intent.putExtra("com.offsec.nhterm.iInitialCommand", "/usr/bin/start-kismet");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
            }
    }
}

