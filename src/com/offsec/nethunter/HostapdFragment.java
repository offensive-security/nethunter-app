package com.offsec.nethunter;

import android.app.Activity;
import android.content.Intent;
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

public class HostapdFragment extends Fragment {

    private String configFilePath = "files/hostapd.conf";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String cacheDir;

    public HostapdFragment() {

    }

    public static HostapdFragment newInstance(int sectionNumber) {
        HostapdFragment fragment = new HostapdFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
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
        ((AppNavHomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        if (isAdded()) {
            cacheDir = getActivity().getCacheDir().toString();
        }
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
        String[] command = {"su -c '" + cacheDir + "/bootkali hostapd start'"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Hostapd started!");
    }

    public void stopHostapd() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"su -c '" + cacheDir + "/bootkali hostapd stop'"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Hostapd stopped!");
    }

    private void loadOptions(final View rootView) {
        String text = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);
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
        String source = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);
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

        boolean r = ((AppNavHomeActivity) getActivity()).updateConfigFile(configFilePath, source);
        if (r) {
            ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
        } else {
            ((AppNavHomeActivity) getActivity()).showMessage("There was a problem with updating options!");
        }
    }
}

