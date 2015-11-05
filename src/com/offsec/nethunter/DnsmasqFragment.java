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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnsmasqFragment extends Fragment {

    private String configFilePath = "files/configs/dnsmasq.conf";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String fileDir;


    public DnsmasqFragment() {

    }

    public static DnsmasqFragment newInstance(int sectionNumber) {
        DnsmasqFragment fragment = new DnsmasqFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dnsmasq, container, false);
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
        if (isAdded()) {
            fileDir = getActivity().getFilesDir().toString() + "/scripts";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOptions(getView().getRootView());
    }


    private void loadOptions(final View rootView) {
        String text = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);

        /*
        * Addresses
		*/
        EditText address1 = (EditText) rootView.findViewById(R.id.address1);
        EditText address2 = (EditText) rootView.findViewById(R.id.address2);
        String regExPatAddress = "^#{0,1}address=(.*)$";
        Pattern patternAddress = Pattern.compile(regExPatAddress, Pattern.MULTILINE);
        Matcher matcherAddress = patternAddress.matcher(text);

        ArrayList<String> addresses = new ArrayList<String>();
        while (matcherAddress.find()) {
            addresses.add(matcherAddress.group(1));
        }
        Integer a = 0;
        for (String item : addresses) {
            a++;
            if (a.equals(1)) {
                address1.setText(item);
            }
            if (a.equals(2)) {
                address2.setText(item);
            }
        }
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
         * dhcp range
         */
        EditText dhcpRange = (EditText) rootView.findViewById(R.id.dhcpRange);
        String regExpatDhcpRange = "^dhcp-range=(.*)$";
        Pattern patternDhcpRange = Pattern.compile(regExpatDhcpRange, Pattern.MULTILINE);
        Matcher matcherDhcpRange = patternDhcpRange.matcher(text);
        if (matcherDhcpRange.find()) {
            String dhcpRangeValue = matcherDhcpRange.group(1);
            dhcpRange.setText(dhcpRangeValue);
        }
        /*
         * dhcp options
         */
        EditText dhcpOption1 = (EditText) rootView.findViewById(R.id.dhcpOption1);
        EditText dhcpOption2 = (EditText) rootView.findViewById(R.id.dhcpOption2);
        String regExPatDhcpOption = "dhcp-option=(.*)$";
        Pattern patternDhcpOption = Pattern.compile(regExPatDhcpOption, Pattern.MULTILINE);
        Matcher matcherDhcpOption = patternDhcpOption.matcher(text);

        ArrayList<String> dhcpOptions = new ArrayList<String>();
        while (matcherDhcpOption.find()) {
            dhcpOptions.add(matcherDhcpOption.group(1));
        }
        Integer b = 0;
        for (String item : dhcpOptions) {
            b++;
            if (b.equals(1)) {
                dhcpOption1.setText(item);
            }
            if (b.equals(2)) {
                dhcpOption2.setText(item);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dnsmasq, menu);
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
                getActivity().startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateOptions(View arg0) {

        EditText address1 = (EditText) getActivity().findViewById(R.id.address1);
        EditText address2 = (EditText) getActivity().findViewById(R.id.address2);
        EditText ifc = (EditText) getActivity().findViewById(R.id.ifc);
        EditText dhcpRange = (EditText) getActivity().findViewById(R.id.dhcpRange);
        EditText dhcpOption1 = (EditText) getActivity().findViewById(R.id.dhcpOption1);
        EditText dhcpOption2 = (EditText) getActivity().findViewById(R.id.dhcpOption2);

        String source = ((AppNavHomeActivity) getActivity()).readConfigFile(configFilePath);
        String regExPatAddress = "^#{0,1}address=(.*)$";
        Pattern patternAddress = Pattern.compile(regExPatAddress, Pattern.MULTILINE);
        Matcher matcherAddress = patternAddress.matcher(source);

        Integer a = 0;
        while (matcherAddress.find()) {
            a++;
            if (a.equals(1)) {
                if (matcherAddress.group(0).startsWith("#")) {
                    source = source.replace(matcherAddress.group(0), "#address=" + address1.getText().toString());
                } else {
                    source = source.replace(matcherAddress.group(0), "address=" + address1.getText().toString());
                }
            }
            if (a.equals(2)) {
                if (matcherAddress.group(0).startsWith("#")) {
                    source = source.replace(matcherAddress.group(0), "#address=" + address2.getText().toString());
                } else {
                    source = source.replace(matcherAddress.group(0), "address=" + address2.getText().toString());
                }
            }
        }
        source = source.replaceAll("(?m)interface=(.*)$", "interface=" + ifc.getText().toString());
        source = source.replaceAll("(?m)dhcp-range=(.*)$", "dhcp-range=" + dhcpRange.getText().toString());

        String regExPatDhcpOption = "dhcp-option=(.*)$";
        Pattern patternDhcpOption = Pattern.compile(regExPatDhcpOption, Pattern.MULTILINE);
        Matcher matcherDhcpOption = patternDhcpOption.matcher(source);

        a = 0;
        while (matcherDhcpOption.find()) {
            a++;
            if (a.equals(1)) {
                source = source.replace(matcherDhcpOption.group(0), "dhcp-option=" + dhcpOption1.getText().toString());
            }
            if (a.equals(2)) {
                source = source.replace(matcherDhcpOption.group(0), "dhcp-option=" + dhcpOption2.getText().toString());
            }
        }
        Boolean r = ((AppNavHomeActivity) getActivity()).updateConfigFile(configFilePath, source);
        if (r) {
            ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
        } else {
            ((AppNavHomeActivity) getActivity()).showMessage("Options not updated!");
        }
    }

    public void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"su -c '" + fileDir + "/bootkali dnsmasq start'"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Dnsmasq started!");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"su -c '" + fileDir + "/bootkali dnsmasq stop'"};
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Dnsmasq stopped!");
    }
}
