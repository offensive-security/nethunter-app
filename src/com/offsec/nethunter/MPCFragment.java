package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;

import java.util.Locale;

import androidx.fragment.app.Fragment;

public class MPCFragment extends Fragment {

    private String typeVar;
    private String callbackTypeVar;
    private String payloadVar;
    private String callbackVar;
    private String stagerVar;

    NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public MPCFragment() {
    }

    public static MPCFragment newInstance(int sectionNumber) {
        MPCFragment fragment = new MPCFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.payload_maker, container, false);
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();


        // Payload Type Spinner
        Spinner typeSpinner = rootView.findViewById(R.id.mpc_type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_type_array, R.layout.payload_maker_item);
        //typeAdapter.setDropDownViewResource(R.layout.payload_maker_item);
        typeSpinner.setAdapter(typeAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        // usually the 1st value of spinner
        typeVar = "asp";
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                switch (pos) {
                    case 0:
                        typeVar = "asp";
                        break;
                    case 1:
                        typeVar = "aspx";
                        break;
                    case 2:
                        typeVar = "bash";
                        break;
                    case 3:
                        typeVar = "java";
                        break;
                    case 4:
                        typeVar = "linux";
                        break;
                    case 5:
                        typeVar = "osx";
                        break;
                    case 6:
                        typeVar = "perl";
                        break;
                    case 7:
                        typeVar = "php";
                        break;
                    case 8:
                        typeVar = "powershell";
                        break;
                    case 9:
                        typeVar = "python";
                        break;
                    case 10:
                        typeVar = "tomcat";
                        break;
                    case 11:
                        typeVar = "windows";
                        break;
                    case 12:
                        typeVar = "apk";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Payload Spinner
        Spinner payloadSpinner = rootView.findViewById(R.id.mpc_payload_spinner);
        ArrayAdapter<CharSequence> payloadAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_payload_array, R.layout.payload_maker_item);
        //typeAdapter.setDropDownViewResource(R.layout.payload_maker_item);
        payloadSpinner.setAdapter(payloadAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        payloadVar = "msf";
        payloadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if (selectedItemText.equals("MSF")) {
                    payloadVar = "msf";
                } else if (selectedItemText.equals("CMD")) {
                    payloadVar = "cmd";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Callback Spinner
        Spinner callbackSpinner = rootView.findViewById(R.id.mpc_callback_spinner);
        ArrayAdapter<CharSequence> callbackAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_callback_array, R.layout.payload_maker_item);
        //typeAdapter.setDropDownViewResource(R.layout.payload_maker_item);
        callbackSpinner.setAdapter(callbackAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        callbackVar = "reverse";
        callbackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if (selectedItemText.equals("Reverse")) {
                    callbackVar = "reverse";
                } else if (selectedItemText.equals("Bind")) {
                    callbackVar = "bind";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Stager Spinner
        Spinner stageSpinner = rootView.findViewById(R.id.mpc_stage_spinner);
        ArrayAdapter<CharSequence> stagerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_stage_array, R.layout.payload_maker_item);
        //typeAdapter.setDropDownViewResource(R.layout.payload_maker_item);
        stageSpinner.setAdapter(stagerAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        stagerVar = "staged";
        stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if (selectedItemText.equals("Staged")) {
                    stagerVar = "staged";
                } else if (selectedItemText.equals("Stageless")) {
                    stagerVar = "stageless";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Callback Type SPinner
        Spinner callbackTypeSpinner = rootView.findViewById(R.id.mpc_callbacktype_spinner);
        ArrayAdapter<CharSequence> callbackTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_callbacktype_array, R.layout.payload_maker_item);
        //typeAdapter.setDropDownViewResource(R.layout.payload_maker_item);
        callbackTypeSpinner.setAdapter(callbackTypeAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        callbackTypeVar = "tcp";
        callbackTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                //use swich!
                switch (selectedItemText) {
                    case "TCP":
                        callbackTypeVar = "tcp";
                        break;
                    case "HTTP":
                        callbackTypeVar = "http";
                        break;
                    case "HTTPS":
                        callbackTypeVar = "https";
                        break;
                    case "Find Port":
                        callbackTypeVar = "find_port";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Port Text Field
        EditText port = rootView.findViewById(R.id.mpc_port);
        port.setText(R.string.mpc_port_default);
        //final String PortStr = port.getText().toString();

        // Get IP address for IP default IP field
        // http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
        WifiManager wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format(Locale.getDefault(),"%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        // IP Text Field
        EditText ipaddress = rootView.findViewById(R.id.mpc_ip_address);
        ipaddress.setText(ip);
        //final String IPAddressStr = ipaddress.getText().toString();
        // this should not be assigned like that
        // the vaue is dinamic, create a gettet (getCmd())
        //cmd = typeVar + " " + ipaddress.getText() + " " + port.getText() + " " + payloadVar + " " + callbackVar + " " + " " + stagerVar + " " + callbackTypeVar;

        Log.d("start cmd values", getCmd(rootView));

        // Buttons
        addClickListener(R.id.mpc_GenerateSDCARD, v -> {
            Log.d("thecmd", "cd /sdcard/; msfpc " + getCmd(rootView));
            intentClickListener_NH("cd /sdcard/; msfpc " + getCmd(rootView)); // since is a kali command we can send it as is
        }, rootView);

        addClickListener(R.id.mpc_GenerateHTTP, v -> {
            Log.d("thecmd", "cd /var/www/html; msfpc " + getCmd(rootView));
            intentClickListener_NH("cd /var/www/html; msfpc " + getCmd(rootView)); // since is a kali command we can send it as is
        }, rootView);

        return rootView;
    }

    private String getCmd(View rootView) {
        EditText ipaddress = rootView.findViewById(R.id.mpc_ip_address);
        EditText port = rootView.findViewById(R.id.mpc_port);
        return typeVar + " " + ipaddress.getText() + " " + port.getText() + " " + payloadVar + " " + callbackVar + " " + " " + stagerVar + " " + callbackTypeVar;
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
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