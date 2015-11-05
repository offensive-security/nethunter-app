package com.offsec.nethunter;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Locale;
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
        TextView buildInfo1 = (TextView) rootView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = (TextView) rootView.findViewById(R.id.buildinfo2);
        TextView licenseView = (TextView) rootView.findViewById(R.id.licenseInfo);
        licenseView.setMovementMethod(LinkMovementMethod.getInstance());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        ip.setFocusable(false);

        buildInfo1.setText("Version: " + BuildConfig.VERSION_NAME + " (" + android.os.Build.TAGS + ")");
        buildInfo2.setText("Built by " + BuildConfig.BUILD_NAME + " at " + sdf.format(BuildConfig.BUILD_TIME));
        addClickListener(R.id.button1, new View.OnClickListener() {
            public void onClick(View v) {
                getExternalIp();
            }
        }, rootView);
        getInterfaces(rootView);
        getHid(rootView);

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

    private void getInterfaces(View rootView) {
        final TextView interfaces = (TextView) rootView.findViewById(R.id.editText1);
        interfaces.setText("Detecting interface...");
        new Thread(new Runnable() {
            public void run() {
                if (interfaces != null) {
                    ShellExecuter exe = new ShellExecuter();
                    String command[] = {"sh", "-c", "netcfg |grep UP |grep -v ^lo|awk -F\" \" '{print $1\"\t\" $3}'"};
                    final String outp = exe.Executer(command);
                    //Logger.appendLog(outp1);
                    interfaces.post(new Runnable() {
                        @Override
                        public void run() {
                            interfaces.setText(outp);
                            interfaces.setFocusable(false);
                        }
                    });
                }
            }
        }).start();
    }

    private void getHid(View rootView) {
        final TextView hid = (TextView) rootView.findViewById(R.id.editText3);

        new Thread(new Runnable() {
            public void run() {
                if (hid != null) {
                    hid.setText("Detecting HID support...");
                    ShellExecuter exe = new ShellExecuter();
                    String command[] = {"sh", "-c", "ls /dev/hidg*"};
                    final String outp = exe.Executer(command);
                    //Logger.appendLog(outp1);
                    hid.post(new Runnable() {
                        @Override
                        public void run() {
                            hid.setText(outp);
                            hid.setFocusable(false);
                        }
                    });
                }
            }
        }).start();
    }
}