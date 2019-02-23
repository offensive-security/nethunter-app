package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class NmapFragment extends Fragment {

    private static final String TAG = "NMAPFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    // Building command line
    private static final ArrayList<String> CommandComposed = new ArrayList<>();

    // Nmap switches
    private String net_interface;
    private String time_template;
    private String searchall;
    private String OSdetect;
    private String udpscan;
    private String ipv6check;
    private String technique;
    private String sv;
    private String MySearch;
    private String Ports;
    private String fastmode;
    private String topports;

    private EditText searchBar;
    private EditText portsBar;

    NhPaths nh;

    public NmapFragment() {
    }

    public static NmapFragment newInstance(int sectionNumber) {
        NmapFragment fragment = new NmapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.nmap, container, false);

        // Default advanced options as invisible
        final LinearLayout AdvLayout = rootView.findViewById(R.id.nmap_adv_layout);
        AdvLayout.setVisibility(View.GONE);

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();

        // Switch to activate open/close of advanced options
        Switch advswitch = rootView.findViewById(R.id.nmap_adv_switch);
        advswitch.setChecked(false);
        advswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.d(TAG, "Advanced Options Open");
                AdvLayout.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "Advanced Options Closed");
                AdvLayout.setVisibility(View.GONE);
            }
        });

        final Button searchButton = rootView.findViewById(R.id.nmap_scan_button);
        searchButton.setOnClickListener(
                view -> getCmd());


        // NMAP Interface Spinner
        Spinner typeSpinner = rootView.findViewById(R.id.nmap_int_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nmap_interface_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        net_interface = "wlan0";
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                switch (pos) {
                    case 0:
                        removeFromCmd(net_interface);
                        break;
                    case 1:
                        removeFromCmd(net_interface);
                        net_interface = " -e wlan0";
                        addToCmd(net_interface);
                        break;
                    case 2:
                        removeFromCmd(net_interface);
                        net_interface = " -e wlan1";
                        addToCmd(net_interface);
                        break;
                    case 3:
                        removeFromCmd(net_interface);
                        net_interface = " -e eth0";
                        addToCmd(net_interface);
                        break;
                    case 4:
                        removeFromCmd(net_interface);
                        net_interface = " -e rndis0";
                        addToCmd(net_interface);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // NMAP Technique Spinner
        Spinner techSpinner = rootView.findViewById(R.id.nmap_scan_tech_spinner);
        ArrayAdapter<CharSequence> techAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nmap_scantechnique_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        techSpinner.setAdapter(techAdapter);
        techSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                switch (pos) {
                    case 0:
                        removeFromCmd(technique);
                        break;
                    case 1:
                        removeFromCmd(technique);
                        technique = " -sS";
                        addToCmd(technique);
                        break;
                    case 2:
                        removeFromCmd(technique);
                        technique = " -sT";
                        addToCmd(technique);
                        break;
                    case 3:
                        removeFromCmd(technique);
                        technique = " -sA";
                        addToCmd(technique);
                        break;
                    case 4:
                        removeFromCmd(technique);
                        technique = " -sW";
                        addToCmd(technique);
                        break;
                    case 5:
                        removeFromCmd(technique);
                        technique = " -sM";
                        addToCmd(technique);
                        break;
                    case 6:
                        removeFromCmd(technique);
                        technique = " -sN";
                        addToCmd(technique);
                        break;
                    case 7:
                        removeFromCmd(technique);
                        technique = " -sF";
                        addToCmd(technique);
                        break;
                    case 8:
                        removeFromCmd(technique);
                        technique = " -sX";
                        addToCmd(technique);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        // Search button
        addClickListener(v -> intentClickListener_NH("nmap " + getCmd()), rootView);


        // NMAP Timing Spinner
        Spinner timeSpinner = rootView.findViewById(R.id.nmap_timing_spinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nmap_timing_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
                switch (pos) {
                    case 0:
                        removeFromCmd(time_template);
                        break;
                    case 1:
                        removeFromCmd(time_template);
                        time_template = " -T 0";
                        addToCmd(time_template);
                        break;
                    case 2:
                        removeFromCmd(time_template);
                        time_template = " -T 1";
                        addToCmd(time_template);
                        break;
                    case 3:
                        removeFromCmd(time_template);
                        time_template = " -T 2";
                        addToCmd(time_template);
                        break;
                    case 4:
                        removeFromCmd(time_template);
                        time_template = " -T 3";
                        addToCmd(time_template);
                        break;
                    case 5:
                        removeFromCmd(time_template);
                        time_template = " -T 4";
                        addToCmd(time_template);
                        break;
                    case 6:
                        removeFromCmd(time_template);
                        time_template = " -T 5";
                        addToCmd(time_template);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        final CheckBox allCheckbox = rootView.findViewById(R.id.nmap_all_check);
        allCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                searchall = " -A";
                addToCmd(searchall);
                Log.d(TAG, searchall);
            } else {
                removeFromCmd(searchall);
                Log.d(TAG, searchall);
            }

        });

        // Checkbox for Fastmode
        final CheckBox fastmodeCheckbox = rootView.findViewById(R.id.nmap_fastmode_check);
        View.OnClickListener checkBoxListener = v -> {
            if (fastmodeCheckbox.isChecked()) {
                fastmode = " -F";
                addToCmd(fastmode);
            } else {
                removeFromCmd(fastmode);
            }
        };
        fastmodeCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for Ping Scan only
        final CheckBox pingCheckbox = rootView.findViewById(R.id.nmap_ping_check);
        checkBoxListener = v -> {
            if (pingCheckbox.isChecked()) {
                fastmode = " -sn";
                addToCmd(fastmode);
            } else {
                removeFromCmd(fastmode);
            }
        };
        pingCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for Top Ports
        final CheckBox topportsCheckbox = rootView.findViewById(R.id.nmap_top_ports_check);
        checkBoxListener = v -> {
            if (topportsCheckbox.isChecked()) {
                topports = " --top-ports 20";
                addToCmd(topports);
            } else {
                removeFromCmd(topports);
            }
        };
        topportsCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for UDP Scan
        final CheckBox udpCheckbox = rootView.findViewById(R.id.nmap_udp_checkbox);
        checkBoxListener = v -> {
            if (udpCheckbox.isChecked()) {
                udpscan = " -sU";
                addToCmd(udpscan);
            } else {
                removeFromCmd(udpscan);
            }
        };
        allCheckbox.setOnClickListener(checkBoxListener);


        // Checkbox for IPv6
        final CheckBox ipv6box = rootView.findViewById(R.id.nmap_ipv6_check);
        checkBoxListener = v -> {
            if (ipv6box.isChecked()) {
                ipv6check = " -6";
                addToCmd(ipv6check);
            } else {
                removeFromCmd(ipv6check);
            }
        };
        ipv6box.setOnClickListener(checkBoxListener);

        // Checkbox for Service Version
        final CheckBox svbox = rootView.findViewById(R.id.nmap_SV_checkbox);
        checkBoxListener = v -> {
            if (svbox.isChecked()) {
                sv = " -sV";
                addToCmd(sv);
            } else {
                removeFromCmd(sv);
            }
        };
        svbox.setOnClickListener(checkBoxListener);

        // Checkbox for OS Detect
        final CheckBox osdetectbox = rootView.findViewById(R.id.nmap_osonly_check);
        checkBoxListener = v -> {
            if (osdetectbox.isChecked()) {
                OSdetect = " -O";
                addToCmd(OSdetect);
            } else {
                removeFromCmd(OSdetect);
            }
        };
        osdetectbox.setOnClickListener(checkBoxListener);

        searchBar = rootView.findViewById(R.id.nmap_searchbar);
        searchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                removeFromCmd(" " + MySearch);
                MySearch = searchBar.getText().toString();
                addToCmd(" " + MySearch);
            }
        });

        // Ports text field
        portsBar = rootView.findViewById(R.id.nmap_ports);
        portsBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                removeFromCmd(" -p" + Ports);
                Ports = portsBar.getText().toString();
                addToCmd(" -p" + Ports);
            }
        });

        return rootView;
    }

    private String getCmd() {
        String genCmd = "";
        for (int j = CommandComposed.size() - 1; j >= 0; j--) {
            genCmd = genCmd + CommandComposed.get(j);
        }
        //Log.d("NMAP SQL:", "nmap --script sqlite-output --script-args=dbname=/tmp/scan.sqlite,dbtable=scandata " + genCmd);
        Log.d("NMAP CMD OUTPUT: ", "nmap " + genCmd);

        return genCmd;
    }

    private static void cleanCmd() {
        if (CommandComposed.size() > 0) {
            CommandComposed.subList(0, CommandComposed.size()).clear();
        }
    }

    private static void addToCmd(String opt) {
        CommandComposed.add(opt);
    }

    private static void removeFromCmd(String opt) {
        for (int j = CommandComposed.size() - 1; j >= 0; j--) {
            if (CommandComposed.get(j).equals(opt))
                CommandComposed.remove(j);
        }
    }

    private void addClickListener(View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(R.id.nmap_scan_button).setOnClickListener(onClickListener);
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
