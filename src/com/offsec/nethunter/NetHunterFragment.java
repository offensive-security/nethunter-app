package com.offsec.nethunter;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetHunterFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String IP_REGEX = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
    private static final Pattern IP_REGEX_PATTERN = Pattern.compile(IP_REGEX);
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */


    public NetHunterFragment() {

    }

    public static NetHunterFragment newInstance(int sectionNumber) {
        NetHunterFragment fragment = new NetHunterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.nethunter, container, false);

        TextView ip = (TextView) rootView.findViewById(R.id.editText2);
        ip.setFocusable(false);
        addClickListener(R.id.button1, new View.OnClickListener() {
            public void onClick(View v) {
                getExternalIp();
            }
        }, rootView);
        getInterfaces(rootView);

        return rootView;
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

    private void getExternalIp() {

        final TextView ip = (TextView) getActivity().findViewById(R.id.editText2);
        ip.setText("Please wait...");

        new Thread(new Runnable() {
            StringBuilder result = new StringBuilder();

            public void run() {

                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    URLConnection urlcon = new URL("https://api.ipify.org").openConnection();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                } catch (Exception e) {
                    result.append("Check connection!");
                }
                final String done;
                Matcher p = IP_REGEX_PATTERN.matcher(result.toString());
                if (p.matches() || result.toString().equals("Check connection!")) {
                    done = result.toString();
                } else {
                    done = "Invalid IP!";
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        ip.setText(done);
                    }
                });
            }
        }).start();
    }

    private void getInterfaces(final View rootView) {
        // 1 thread, 2 commands
        final TextView netIfaces = (TextView) rootView.findViewById(R.id.editText1); // NET IFACES
        final TextView hidIfaces = (TextView) rootView.findViewById(R.id.editText3); // HID IFACES
        // Dont move this inside the thread. (Will throw a null pointer.)
        netIfaces.setText("Detecting Network interfaces...");
        hidIfaces.setText("Detecting HID interfaces...");

        new Thread(new Runnable() {
            public void run() {
                    ShellExecuter exe = new ShellExecuter();
                    String commandNET[] = {"sh", "-c", "netcfg |grep UP |grep -v ^lo|awk -F\" \" '{print $1\"\t\" $3}'"};
                    String commandHID[] = {"sh", "-c", "ls /dev/hidg*"};

                    final String outputNET = exe.Executer(commandNET);
                    final String outputHID = exe.Executer(commandHID);

                    netIfaces.post(new Runnable() {
                        @Override
                        public void run() {
                            if (outputNET.equals("")) {
                                netIfaces.setText("No interfaces detected");
                            } else {
                                netIfaces.setText(outputNET);
                            }
                            netIfaces.setFocusable(false);
                            if (outputHID.equals("")) {
                                hidIfaces.setText("No interfaces detected");
                            } else {
                                hidIfaces.setText(outputHID);
                            }
                            hidIfaces.setFocusable(false);
                        }
                    });
                }
        }).start();
    }

}