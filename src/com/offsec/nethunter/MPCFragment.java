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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class MPCFragment extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    String typeVar;
    String callbackTypeVar;
    String payloadVar;
    String callbackVar;
    String stagerVar;
    String cmd;

    NhUtil nh;
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
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();


        // Payload Type Spinner
        Spinner typeSpinner = (Spinner) rootView.findViewById(R.id.mpc_type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        // usually the 1st value of spinner
        typeVar = "asp";
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                //use swich!

                if(selectedItemText.equals("ASP")) {
                    typeVar = "asp";
                }
                else if(selectedItemText.equals("ASPX")) {
                    typeVar = "aspx";
                }
                else if(selectedItemText.equals("Bash [.sh]")) {
                    typeVar = "bash";
                }
                else if(selectedItemText.equals("Java [.jsp]")) {
                    typeVar = "java";
                }
                else if(selectedItemText.equals("Linux [.elf]")) {
                    typeVar = "linux";
                }
                else if(selectedItemText.equals("OSX [.macho]")) {
                    typeVar = "osx";
                }
                else if(selectedItemText.equals("Perl [.pl]")) {
                    typeVar = "perl";
                }
                else if(selectedItemText.equals("PHP")) {
                    typeVar = "php";
                }
                else if(selectedItemText.equals("Powershell [.ps1]")) {
                    typeVar = "powershell";
                }
                else if(selectedItemText.equals("Python [.py]")) {
                    typeVar = "tomcat";
                }
                else if(selectedItemText.equals("Tomcat [.war]")) {
                    typeVar = "windows";
                }
                else if(selectedItemText.equals("Windows [.exe]")) {
                    typeVar = "asp";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                //Another interface callback
            }
        });

        // Payload Spinner
        Spinner payloadSpinner = (Spinner) rootView.findViewById(R.id.mpc_payload_spinner);
        ArrayAdapter<CharSequence> payloadAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_payload_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payloadSpinner.setAdapter(payloadAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        payloadVar = "msf";
        payloadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if(selectedItemText.equals("MSF")) {
                    payloadVar = "msf";
                }
                else if(selectedItemText.equals("CMD")) {
                    payloadVar = "cmd";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Callback Spinner
        Spinner callbackSpinner = (Spinner) rootView.findViewById(R.id.mpc_callback_spinner);
        ArrayAdapter<CharSequence> callbackAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_callback_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        callbackSpinner.setAdapter(callbackAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        callbackVar = "reverse";
        callbackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if(selectedItemText.equals("Reverse")) {
                    callbackVar = "reverse";
                }
                else if(selectedItemText.equals("Bind")) {
                    callbackVar = "bind";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Stager Spinner
        Spinner stageSpinner = (Spinner) rootView.findViewById(R.id.mpc_stage_spinner);
        ArrayAdapter<CharSequence> stagerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_stage_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stageSpinner.setAdapter(stagerAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        stagerVar = "staged";
        stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                if(selectedItemText.equals("Staged")) {
                    stagerVar = "staged";
                }
                else if(selectedItemText.equals("Stageless")) {
                    stagerVar = "stageless";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Callback Type SPinner
        Spinner callbackTypeSpinner = (Spinner) rootView.findViewById(R.id.mpc_callbacktype_spinner);
        ArrayAdapter<CharSequence> callbackTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.mpc_callbacktype_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        callbackTypeSpinner.setAdapter(callbackTypeAdapter);
        //Give it a initial value: this value stands until onItemSelected is fired
        callbackTypeVar = "tcp";
        callbackTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                Log.d("Slected: ", selectedItemText);
                //use swich!
                if (selectedItemText.equals("TCP")) {
                    callbackTypeVar = "tcp";
                } else if (selectedItemText.equals("HTTP")) {
                    callbackTypeVar = "http";
                } else if (selectedItemText.equals("HTTPS")) {
                    callbackTypeVar = "https";
                } else if (selectedItemText.equals("Find Port")) {
                    callbackTypeVar = "find_port";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Port Text Field
        EditText port = (EditText)rootView.findViewById(R.id.mpc_port);
        port.setText("443");
        //final String PortStr = port.getText().toString();

        // Get IP address for IP default IP field
        // http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
        WifiManager wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        // IP Text Field
        EditText ipaddress = (EditText)rootView.findViewById(R.id.mpc_ip_address);
        ipaddress.setText(ip);
        //final String IPAddressStr = ipaddress.getText().toString();
        // this should not be assigned like that
        // the vaue is dinamic, create a gettet (getCmd())
        //cmd = typeVar + " " + ipaddress.getText() + " " + port.getText() + " " + payloadVar + " " + callbackVar + " " + " " + stagerVar + " " + callbackTypeVar;

        Log.d("start cmd values", getCmd(rootView));

        // Buttons
        addClickListener(R.id.mpc_GenerateSDCARD, new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("thecmd", "cd /sdcard/; mpc " + getCmd(rootView));
                intentClickListener_NH("cd /sdcard/; mpc " + getCmd(rootView)); // since is a kali command we can send it as is
            }
        }, rootView);

        addClickListener(R.id.mpc_GenerateHTTP, new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("thecmd", "cd /var/www/html; mpc " + getCmd(rootView));
                intentClickListener_NH("cd /var/www/html; mpc " + getCmd(rootView)); // since is a kali command we can send it as is
            }
        }, rootView);

        return rootView;
    }

    private String getCmd(View rootView){
        EditText ipaddress = (EditText)rootView.findViewById(R.id.mpc_ip_address);
        EditText port = (EditText)rootView.findViewById(R.id.mpc_port);
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