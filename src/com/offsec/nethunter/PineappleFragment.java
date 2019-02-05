package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import androidx.fragment.app.Fragment;

public class PineappleFragment extends Fragment {

    private static final String TAG = "PineappleFragment";

    private static NhPaths nh;
    private String start_type = "start ";
    private String proxy_type;

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
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();

        nh = new NhPaths();
        Log.d(TAG, nh.APP_SCRIPTS_PATH);

        // Checkbox for No Upstream
        final CheckBox noupCheckbox = rootView.findViewById(R.id.pineapple_noup);
        View.OnClickListener checkBoxListener = v -> {
            if (noupCheckbox.isChecked()) {
                start_type = "start_noup ";
            } else {
                start_type = "start ";
            }
        };
        noupCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for Transparent Proxy
        final CheckBox transCheckbox = rootView.findViewById(R.id.pineapple_transproxy);
        checkBoxListener = v -> {
            if (noupCheckbox.isChecked()) {
                proxy_type = " start_proxy ";
            } else {
                proxy_type = "";
            }
        };
        transCheckbox.setOnClickListener(checkBoxListener);

        // Start Button
        addClickListener(R.id.pineapple_start_button, v -> {
            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/pine-nano " + start_type + startConnection(rootView) + proxy_type + "'";
                Log.d(TAG, command);
                exe.RunAsRootOutput(command);
            }).start();
            nh.showMessage("Starting eth0 connection");
        }, rootView);

        // Stop|Close Button
        addClickListener(R.id.pineapple_close_button, v -> {
            new Thread(() -> {
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/pine-nano stop'";
                Log.d(TAG, command);
                exe.RunAsRootOutput(command);
            }).start();
            nh.showMessage("Bringing down eth0 conneciton");
        }, rootView);

        return rootView;
    }

    private String startConnection(View rootView) {
        // Port Text Field
        EditText port = rootView.findViewById(R.id.pineapple_webport);

        // Gateway IP Text Field
        EditText gateway_ip = rootView.findViewById(R.id.pineapple_gatewayip);

        // Client IP Text Field
        EditText web_ip = rootView.findViewById(R.id.pineapple_clientip);

        // CIDR Text Field
        EditText CIDR = rootView.findViewById(R.id.pineapple_cidr);

        // Pineapple CIDR Text Field
        return web_ip.getText() + " " + CIDR.getText() + " " + gateway_ip.getText() + " " + port.getText();
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }
}