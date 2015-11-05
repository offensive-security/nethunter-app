package com.offsec.nethunter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.app.Fragment;


public class BadusbFragment extends Fragment {

    private String configFilePath;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public BadusbFragment() {
        if (Build.VERSION.SDK_INT >= 21) {
            configFilePath = "files/configs/startbadusb-lollipop.sh";
        } else {
            configFilePath = "files/configs/startbadusb-kitkat.sh";
        }
    }

    public static BadusbFragment newInstance(int sectionNumber) {
        BadusbFragment fragment = new BadusbFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.badusb, container, false);
        loadOptions(rootView);
        final Button button = (Button) rootView.findViewById(R.id.updateOptions);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateOptions(v);
            }
        });
        setHasOptionsMenu(true);
        return rootView;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onResume() {
        super.onResume();
        loadOptions(getView().getRootView());
    }


    private void loadOptions(View rootView) {
        String text = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);

        EditText ifc = (EditText) rootView.findViewById(R.id.ifc);
        String regExpatInterface = "^INTERFACE=(.*)$";
        Pattern pattern = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String ifcValue = matcher.group(1);
            ifc.setText(ifcValue);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.badusb, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_service:
                start();
                return true;
            case R.id.stop_service:
                stop();
                return true;
            case R.id.source_button:
                Intent i = new Intent(getActivity(), EditSourceActivity.class);
                i.putExtra("path", configFilePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateOptions(View arg0) {
        String source = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);
        EditText ifc = (EditText) getActivity().findViewById(R.id.ifc);
        source = source.replaceAll("(?m)^INTERFACE=(.*)$", "INTERFACE=" + ifc.getText().toString());
        Boolean r = ((AppNavHomeActivity) getActivity()).updateConfigFile(configFilePath, source);
        if (r) {
            ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
        } else {
            ((AppNavHomeActivity) getActivity()).showMessage("Options not updated!");
        }
    }

    public void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = "start-badusb-lollipop &> /sdcard/files/badusb.log &";
        } else {
            command[0] = "start-badusb-kitkat &> /sdcard/files/badusb.log &";
        }
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("BadUSB attack started!");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = "stop-badusb-lollipop";
        } else {
            command[0] = "stop-badusb-kitkat";
        }
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("BadUSB attack stopped!");
    }
}