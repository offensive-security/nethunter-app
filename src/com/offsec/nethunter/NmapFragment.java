package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.offsec.nethunter.utils.NhPaths;

import com.thomashaertel.widget.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NmapFragment  extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    private static final String TAG = "NMAPFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    Switch advswitch;
    View.OnClickListener checkBoxListener;

    // Building command line
    static ArrayList<String> CommandComposed = new ArrayList<>();

    // Multi-dropdown spinner
    private MultiSpinner tech_spinner;
    private ArrayAdapter<String> options;

    // Nmap switches
    String net_interface;
    String time_template;
    String All;
    String OSdetect;
    String ipv6check;
    String MySearch;

    EditText searchBar;

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
        final LinearLayout AdvLayout = (LinearLayout) rootView.findViewById(R.id.nmap_adv_layout);
        AdvLayout.setVisibility(View.GONE);

        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();

        // Switch to activate open/close of advanced options
        advswitch = (Switch) rootView.findViewById(R.id.nmap_adv_switch);
        advswitch.setChecked(false);
        advswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Advanced Options Open");
                    AdvLayout.setVisibility(View.VISIBLE);
               } else {
                    Log.d(TAG, "Advanced Options Closed");
                    AdvLayout.setVisibility(View.GONE);
                }
            }
        });

        final Button searchButton = (Button) rootView.findViewById(R.id.nmap_scan_button);
        searchButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        getCmd();
                    }
                });

        // NMAP Interface Spinner
        Spinner typeSpinner = (Spinner) rootView.findViewById(R.id.nmap_int_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nmap_interface_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        net_interface = "wlan0";
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
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
            public void onNothingSelected(AdapterView<?> parent){
                //Another interface callback
            }
        });


        // NMAP Timming Spinner
        Spinner timeSpinner = (Spinner) rootView.findViewById(R.id.nmap_timing_spinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.nmap_timing_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
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
            public void onNothingSelected(AdapterView<?> parent){
                //Another interface callback
            }
        });

        // Spinner for Scan Technique Selection

        options = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        options.add("TCP SYN");
        options.add("Connect()");
        options.add("ACK");
        options.add("Windows");
        options.add("Maimon");
        options.add("UDP Scan");
        options.add("TCP Null");
        options.add("FIN");
        options.add("XMAS");

        // get spinner and set adapter
        tech_spinner = (MultiSpinner) rootView.findViewById(R.id.ScanTechMulti);
        tech_spinner.setAdapter(options, false, onSelectedListener);
        boolean[] selectedItems = new boolean[options.getCount()];
        selectedItems[0] = true; // // selected first item
        tech_spinner.setSelected(selectedItems);

        // Checkbox for ALL Version/OS Checkbox
        final CheckBox allCheckbox = (CheckBox) rootView.findViewById(R.id.nmap_A_check);
        checkBoxListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(allCheckbox.isChecked()) {
                    All = " -A";
                    addToCmd(All);
                }else{
                    removeFromCmd(All);
                }
            }
        };
        allCheckbox.setOnClickListener(checkBoxListener);

        // Checkbox for IPv6
        final CheckBox ipv6box = (CheckBox) rootView.findViewById(R.id.nmap_ipv6_check);
        checkBoxListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(ipv6box.isChecked()) {
                    ipv6check = " -6";
                    addToCmd(ipv6check);
                }else{
                    removeFromCmd(ipv6check);
                }
            }
        };
        ipv6box.setOnClickListener(checkBoxListener);


        // Checkbox for OS Detect
        final CheckBox osdetectbox = (CheckBox) rootView.findViewById(R.id.nmap_osonly_check);
        checkBoxListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(osdetectbox.isChecked()) {
                    OSdetect = " -O";
                    addToCmd(OSdetect);
                }else{
                    removeFromCmd(OSdetect);
                }
            }
        };
        osdetectbox.setOnClickListener(checkBoxListener);

        searchBar = (EditText) rootView.findViewById(R.id.nmap_searchbar);
        searchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                removeFromCmd(MySearch);
                MySearch = searchBar.getText().toString();
                addToCmd(MySearch);
            }
        });

        return rootView;
    }

    private MultiSpinner.MultiSpinnerListener onSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    builder.append(options.getItem(i)).append(" ");
                }
                Log.d(TAG, builder.toString());
            }
        }
    };

    private String getCmd(){
        String genCmd = "";
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
            genCmd = genCmd + CommandComposed.get(j);
        }
        Log.d("NMAP CMD OUTPUT: ", "nmap -oX /tmp/tmp_to_sql.xml " + genCmd);

        return genCmd;
    }
    private static void cleanCmd() {
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
            CommandComposed.remove(j);
        }
    }
    private static void addToCmd(String opt) {
        CommandComposed.add(opt);
    }
    private static void removeFromCmd(String opt) {
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
            if(CommandComposed.get(j).equals(opt))
                CommandComposed.remove(j);
        }
    }
}