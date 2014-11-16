package com.offsec.nethunter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class NetHunterFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

     int ARG_SECTION_NUMBER;
     String ARG_ACTIVITY_NAME;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */


    public NetHunterFragment(int sectionNumber, String activityName) {

        ARG_SECTION_NUMBER = sectionNumber;
        ARG_ACTIVITY_NAME = activityName;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.nethunter, container, false);

        String intf = getInterfaces();
        EditText interfaces = (EditText) rootView.findViewById(R.id.editText1);
        EditText ip = (EditText) rootView.findViewById(R.id.editText2);

        interfaces.setText(intf);
        interfaces.setFocusable(false);
        ip.setFocusable(false);
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
        ((AppNavHomeActivity) activity).onSectionAttached(ARG_SECTION_NUMBER);

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