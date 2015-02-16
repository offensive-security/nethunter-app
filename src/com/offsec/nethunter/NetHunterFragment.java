package com.offsec.nethunter;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

//import android.app.Fragment;

public class NetHunterFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private static final String ARG_SECTION_NUMBER = "section_number";

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

        String intf = getInterfaces();
        EditText interfaces = (EditText) rootView.findViewById(R.id.editText1);
        EditText ip = (EditText) rootView.findViewById(R.id.editText2);
        TextView buildInfo1 = (TextView) rootView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = (TextView) rootView.findViewById(R.id.buildinfo2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        interfaces.setText(intf);
        interfaces.setFocusable(false);
        ip.setFocusable(false);
        buildInfo1.setText("Version: " + BuildConfig.VERSION_NAME);
        buildInfo2.setText("Built by " + BuildConfig.BUILD_NAME + " at " + sdf.format(BuildConfig.BUILD_TIME));
        addClickListener(R.id.button1, new View.OnClickListener() {
            public void onClick(View v) {
                getExternalIp();
            }
        }, rootView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

    private void getExternalIp() {

        final EditText ip = (EditText) getActivity().findViewById(R.id.editText2);
        ip.setText("Please wait...");

        new Thread(new Runnable() {
            public void run() {

                try {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet("http://myip.dnsomatic.com");
                    final HttpResponse response;
                    response = httpclient.execute(httpget);
                    final HttpEntity entity = response.getEntity();

                    if (entity != null) {
                        long len = entity.getContentLength();
                        if (len != -1 && len < 1024) {
                            final String str = EntityUtils.toString(entity);
                            ip.post(new Runnable() {
                                public void run() {
                                    ip.setText(str);
                                }
                            });
                        } else {
                            ip.post(new Runnable() {
                                public void run() {
                                    ip.setText("Response too long or error.");
                                }
                            });
                        }
                    } else {
                        ip.post(new Runnable() {
                            public void run() {
                                ip.setText("Null:" + response.getStatusLine().toString());
                            }
                        });
                    }

                } catch (Exception e) {
                    ip.post(new Runnable() {
                        public void run() {
                            ip.setText("Generic Error");
                        }
                    });
                }
            }
        }).start();
    }

    private String getInterfaces() {
        String command[] = {"sh", "-c", "netcfg |grep UP |grep -v ^lo|awk -F\" \" '{print $1\"\t\" $3}'"};
        ShellExecuter exe = new ShellExecuter();
        return exe.Executer(command);

    }
}