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
import android.widget.Button;
import android.widget.EditText;

public class HostapdFragment extends Fragment {
	private String configFilePath = "files/hostapd.conf";
	int ARG_SECTION_NUMBER;
	String ARG_ACTIVITY_NAME;
	    
	public HostapdFragment(int sectionNumber, String activityName) {
	        ARG_SECTION_NUMBER = sectionNumber;
	        ARG_ACTIVITY_NAME = activityName;
		}
	    
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    	View rootView = inflater.inflate(R.layout.hostapd, container, false);
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
	        ((AppNavHomeActivity) activity).onSectionAttached(ARG_SECTION_NUMBER);
	    }
	    
	    @Override
	    public void onResume() {
	        super.onResume();
	        loadOptions(getView().getRootView());
	    }
	    
	    @Override
	    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        inflater.inflate(R.menu.hostapd, menu);
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle presses on the action bar items
	        switch (item.getItemId()) {
	            case R.id.start_service:
	                startHostapd();
	                return true;
	            case R.id.stop_service:
	                stopHostapd();
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
	    
	    public void startHostapd() {
	        ShellExecuter exe = new ShellExecuter();
	        String[] command = {"start-hostapd &"};
	        exe.RunAsRoot(command);
	        ((AppNavHomeActivity) getActivity()).showMessage("Hostapd started!");
	    }

	    public void stopHostapd() {
	        ShellExecuter exe = new ShellExecuter();
	        String[] command = {"stop-hostapd"};
	        exe.RunAsRoot(command);
	        ((AppNavHomeActivity) getActivity()).showMessage("Hostapd stopped!");
	    }
	    
	    private void loadOptions(final View rootView) {
	        String text = readConfigFile();
	        /*
	         * Interface
	         */
	        EditText ifc = (EditText) rootView.findViewById(R.id.ifc);
	        String regExpatInterface = "^interface=(.*)$";
	        Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
	        Matcher matcherIfc = patternIfc.matcher(text);
	        if (matcherIfc.find()) {
	            String ifcValue = matcherIfc.group(1);
	            ifc.setText(ifcValue);
	        }
	        
	        /*
	         * bssid
	         */
	        EditText bssid = (EditText) rootView.findViewById(R.id.bssid);
	        String regExpatbssid = "^bssid=(.*)$";
	        Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
	        Matcher matcherBssid = patternBssid.matcher(text);
	        if (matcherBssid.find()) {
	            String bssidVal = matcherBssid.group(1);
	            bssid.setText(bssidVal);
	        }
	        /*
	         * ssid
	         */
	        EditText ssid = (EditText) rootView.findViewById(R.id.ssid);
	        String regExpatssid = "^ssid=(.*)$";
	        Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
	        Matcher matcherSsid = patternSsid.matcher(text);
	        if (matcherSsid.find()) {
	            String ssidVal = matcherSsid.group(1);
	            ssid.setText(ssidVal);
	        }
	        /*
	         * channel
	         */
	        EditText channel = (EditText) rootView.findViewById(R.id.channel);
	        String regExpatChannel = "^channel=(.*)$";
	        Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
	        Matcher matcherChannel = patternChannel.matcher(text);
	        if (matcherChannel.find()) {
	            String channelVal = matcherChannel.group(1);
	            channel.setText(channelVal);
	        }
	        /*
	         * enable_karma
	         */
	        EditText enableKarma = (EditText) rootView.findViewById(R.id.enableKarma);
	        String regExpatEnableKarma = "^enable_karma=(.*)$";
	        Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
	        Matcher matcherEnableKarma = patternEnableKarma.matcher(text);
	        if (matcherEnableKarma.find()) {
	            String enableKarmaVal = matcherEnableKarma.group(1);
	            enableKarma.setText(enableKarmaVal);
	        }
	    }
	    
	    public void updateOptions(View arg0) {
	        String source = readConfigFile();
	        EditText ifc = (EditText) getActivity().findViewById(R.id.ifc);
	        EditText bssid = (EditText) getActivity().findViewById(R.id.bssid);
	        EditText ssid = (EditText) getActivity().findViewById(R.id.ssid);
	        EditText channel = (EditText) getActivity().findViewById(R.id.channel);
	        EditText enableKarma = (EditText) getActivity().findViewById(R.id.enableKarma);

	        source = source.replaceAll("(?m)^interface=(.*)$", "interface=" + ifc.getText().toString());
	        source = source.replaceAll("(?m)^bssid=(.*)$", "bssid=" + bssid.getText().toString());
	        source = source.replaceAll("(?m)^ssid=(.*)$", "ssid=" + ssid.getText().toString());
	        source = source.replaceAll("(?m)^channel=(.*)$", "channel=" + channel.getText().toString());
	        source = source.replaceAll("(?m)^enable_karma=(.*)$", "enable_karma=" + enableKarma.getText().toString());

	        updateConfigFile(source);
	        ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
	        
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
}

