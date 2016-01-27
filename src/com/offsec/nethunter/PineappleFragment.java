package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

public class PineappleFragment extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    private static final String TAG = "PineappleFragment";

    static NhPaths nh;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public PineappleFragment() {
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

        nh = new NhPaths();
        Log.d(TAG, nh.APP_SCRIPTS_PATH);

        addClickListener(R.id.pineapple_start_button, new View.OnClickListener() {
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            public void run() {
                                ShellExecuter exe = new ShellExecuter();
                                String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/pine-nano start " + startConnection(rootView) + "'";
                                Log.d(TAG, command);
                                exe.RunAsRootOutput(command);
                            }
                        }).start();
                        nh.showMessage("Starting eth0 connection");
                    }
                }, rootView);

        addClickListener(R.id.pineapple_close_button, new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        ShellExecuter exe = new ShellExecuter();
                        String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/pine-nano stop'";
                        Log.d(TAG, command);
                        exe.RunAsRootOutput(command);
                    }
                }).start();
                nh.showMessage("Bringing down eth0 conneciton");
            }
        }, rootView);

    return rootView;
    }

    private String startConnection(View rootView){
        // Port Text Field
        EditText port = (EditText)rootView.findViewById(R.id.pineapple_webport);

        // Gateway IP Text Field
        EditText gateway_ip = (EditText)rootView.findViewById(R.id.pineapple_gatewayip);

        // Client IP Text Field
        EditText web_ip = (EditText)rootView.findViewById(R.id.pineapple_clientip);

        // CIDR Text Field
        EditText CIDR = (EditText)rootView.findViewById(R.id.pineapple_cidr);

        // Pineapple CIDR Text Field
        return web_ip.getText() + " " + CIDR.getText() + " " +gateway_ip.getText() + " " + port.getText();
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }
}