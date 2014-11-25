package com.offsec.nethunter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class BadusbFragment extends Fragment {
	private int ARG_SECTION_NUMBER;
	String ARG_ACTIVITY_NAME;
	private String configFilePath = "files/startbadusb.sh";
	
	public BadusbFragment(int sectionNumber, String activityName) {
        ARG_SECTION_NUMBER = sectionNumber;
        ARG_ACTIVITY_NAME = activityName;
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.badusb, container, false);
    	loadOptions(rootView);
    	setHasOptionsMenu(true);
    	return rootView;
        
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(ARG_SECTION_NUMBER);
    }
    
    public void onResume() {
        super.onResume();
        loadOptions(getView().getRootView());
    }
    
    
    private void loadOptions(View rootView) {
        String text = readConfigFile();

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
        String source = readConfigFile();
        EditText ifc = (EditText) arg0.findViewById(R.id.ifc);
        source = source.replaceAll("(?m)^INTERFACE=(.*)$", "INTERFACE=" + ifc.getText().toString());
        updateConfigFile(source);
        ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
    }


    public void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"start-badusb &> /sdcard/htdocs/badusb.log &"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("BadUSB attack started!");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"stop-badusb"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("BadUSB attack stopped!");
    }

    private void updateConfigFile(String source) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard, configFilePath);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(source);
            myOutWriter.close();
            fOut.close();
            ((AppNavHomeActivity) getActivity()).showMessage("Source updated!");
        } catch (Exception e) {
            ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
        }
    }

    private String readConfigFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, configFilePath);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e("Nethunter", "exception", e);
        }
        return text.toString();
    }
}