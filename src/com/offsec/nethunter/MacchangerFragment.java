package com.offsec.nethunter;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


public class MacchangerFragment extends Fragment {


    private SharedPreferences sharedpreferences;
    private NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ShellExecuter exe;

    public MacchangerFragment() {

    }

    public static MacchangerFragment newInstance(int sectionNumber) {
        MacchangerFragment fragment = new MacchangerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.macchanger, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (getView() == null) {
            return;
        }
        View v = getView();
        final Spinner interfaceSpinner = v.findViewById(R.id.interface_opts);
        String selectedInterface = interfaceSpinner.getSelectedItem().toString();
        String cleanInterface = selectedInterface.split(" ")[0];
        menu.findItem(R.id.reset_mac).setTitle(String.format("Reset %s MAC", cleanInterface));


    }

    // menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reset_mac:
                resetMac();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nh = new NhPaths();
        exe = new ShellExecuter();
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.macchanger, container, false);
        // get views
        final Spinner interfaceSpinner = rootView.findViewById(R.id.interface_opts);
        final Spinner macModeSpinner = rootView.findViewById(R.id.macchanger_opts);
        final Button setMacButton = rootView.findViewById(R.id.set_mac_button);
        final Button setHostname = rootView.findViewById(R.id.setHostname);
        final EditText phoneName = rootView.findViewById(R.id.phone_nameText);
        if (isOPO() && !sharedpreferences.contains("opo_original_mac")) {
            Editor editor = sharedpreferences.edit();
            editor.putString("opo_original_mac", exe.RunAsRootWithException("cat /sys/devices/fb000000.qcom,wcnss-wlan/wcnss_mac_addr"));
            editor.apply();

        }
        Log.d("opo_original_mac", sharedpreferences.getString("opo_original_mac", ""));
        //###############
        new Thread(() -> {
            final String hostname = getHostname();
            phoneName.post(() -> phoneName.setText(hostname));
        }).start();

        // ############################
        final View mac_layout = rootView.findViewById(R.id.mac_lay1);
        String[] ifacesList = getResources().getStringArray(R.array.interface_opts);
        if (isOldDevice()) {
            ifacesList = delFromArray(ifacesList, ifacesList.length - 2);
        } else {
            ifacesList = delFromArray(ifacesList, ifacesList.length - 1);

        }
        interfaceSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                R.layout.macchanger_ifaces_item, ifacesList));
        final TextView macResult = rootView.findViewById(R.id.macResult);

        final TextView currMac = rootView.findViewById(R.id.currMac);
        final ImageButton reloadMAC = rootView.findViewById(R.id.reloadMAC);
        // index for the arrays
        int current_interface = -1;
        int current_mode = -1;


        // set the last used interface
        for (String cc : getResources().getStringArray(R.array.interface_opts)) {
            current_interface++;
            if (cc.equals(sharedpreferences.getString("interface_opts", cc)))
                break;  // stop adding
        }
        // set interface
        interfaceSpinner.setSelection(current_interface);

        // set the last used mode (same logic)
        for (String cc : getResources().getStringArray(R.array.macchanger_opts)) {
            current_mode++;
            if (cc.equals(sharedpreferences.getString("macchanger_opts", cc))) {
                break;
            }
        }
        // set last used mode
        macModeSpinner.setSelection(current_mode);

        // set listeners on item selected (for both spinners).
        interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String selectedInterface = interfaceSpinner.getSelectedItem().toString();
                String cleanInterface = selectedInterface.split(" ")[0];
                //showMessage("Selected interface: \n" + items);
                Editor editor = sharedpreferences.edit();
                editor.putString("interface_opts", selectedInterface);  // the full text so we can compare later
                editor.apply();


                getCurrentMac(cleanInterface, currMac, setMacButton);  // this gets the current mac of the interface and sets it to the textview
                // update the button text

                if (macModeSpinner.getSelectedItem().toString().equals("Random MAC")) {
                    setMacButton.setText(String.format("Set Random MAC on %s", cleanInterface));
                }
                if (macModeSpinner.getSelectedItem().toString().equals("Custom MAC")) {
                    setMacButton.setText(String.format("Set Custom MAC on %s", cleanInterface));
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }


        });
        setHostname.setOnClickListener(v -> new Thread(() -> {
            setHostname(phoneName.getText().toString());
            v.post(() -> nh.showMessage("Hostname changed"));
        }).start());
        reloadMAC.setOnClickListener(v -> {
            String selectedInterface = interfaceSpinner.getSelectedItem().toString();
            String cleanInterface = selectedInterface.split(" ")[0];
            getCurrentMac(cleanInterface, currMac, setMacButton);
        });

        macModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d("randMac", randomMACAddress());
                // update sharedpreferences
                String items = macModeSpinner.getSelectedItem().toString();
                Editor editor = sharedpreferences.edit();
                editor.putString("macchanger_opts", items);
                editor.apply();

                // update button
                if (items.equals(getResources().getString(R.string.randomMAC))) {
                    setMacButton.setText(String.format("Set Random MAC on %s", interfaceSpinner.getSelectedItem().toString().split(" ")[0]));
                    mac_layout.setVisibility(View.GONE); // hide the linear layout
                    macResult.setText("");
                }

                if (items.equals(getResources().getString(R.string.customMAC))) {
                    setMacButton.setText(String.format("Set Custom MAC on %s", interfaceSpinner.getSelectedItem().toString().split(" ")[0]));
                    mac_layout.setVisibility(View.VISIBLE); // show the linear layout
                    macResult.setText("");

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });
        // on click
        setMacButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
                mWakeLock.acquire();
                // Usage: bootkali macchanger <mac address> <interface> || Random: bootkali macchanger random <interface>
                String command;
                final String selectedDevice = interfaceSpinner.getSelectedItem().toString().split(" ")[0];
                final String macsArray = getMacValues();

                if (macModeSpinner.getSelectedItem().toString().equals("Random MAC")) {
                    if (selectedDevice.equals("wlan0")) {
                        // bacon A0001 one OnePlus
                        if (isOPO()) {
                            command = "settings put global airplane_mode_on 1" +
                                    " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true" +
                                    " && echo \"" + randomMACAddress() + "\" > /sys/devices/fb000000.qcom,wcnss-wlan/wcnss_mac_addr" +
                                    " && sleep 1" +
                                    " && settings put global airplane_mode_on 0" +
                                    " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
                        } else {
                            command = "svc wifi disable" +
                                    " && sleep 2" +
                                    " && svc wifi enable" +
                                    " && sleep 2" +
                                    " && ip link set dev wlan0 address " +
                                    randomMACAddress();
                        }

                        final String finalCommand = command;
                        new Thread(() -> {
                            exe.RunAsRootWithException(finalCommand);
                            macModeSpinner.post(() -> new android.os.Handler().postDelayed(
                                    () -> {
                                        nh.showMessage("Refreshing the current MAC.");
                                        refreshMAc();

                                    }, 1000));
                        }).start();
                    } else {
                        exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + selectedDevice + " down");

                        command = "bootkali macchanger_random " + selectedDevice;
                        exe.RunAsRootWithException(command);
                        // macResult.setText();

                        new android.os.Handler().postDelayed(
                                () -> {
                                    exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + selectedDevice + " up");
                                    nh.showMessage("Refreshing the current MAC.");
                                    refreshMAc();
                                }, 500);
                    }

                }

                if (macModeSpinner.getSelectedItem().toString().equals("Custom MAC")) {
                    if (macsArray != null) {
                        if (macsArray.length() != 17) {
                            nh.showMessage("Invalid custom MAC. Review it.");
                            return;
                        }
                    }
                    if (selectedDevice.equals("wlan0")) {
                        if (isOPO()) {
                            command = "settings put global airplane_mode_on 1" +
                                    " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true" +
                                    " && echo \"" + macsArray + "\" > /sys/devices/fb000000.qcom,wcnss-wlan/wcnss_mac_addr" +
                                    " && sleep 1" +
                                    " && settings put global airplane_mode_on 0" +
                                    " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
                        } else {
                            command = "svc wifi disable && svc wifi enable" +
                                    " && ip link set dev wlan0 address " +
                                    macsArray;
                        }
                        final String finalCommand = command;
                        new Thread(() -> {
                            exe.RunAsRootWithException(finalCommand);
                            macModeSpinner.post(() -> new android.os.Handler().postDelayed(
                                    () -> {
                                        nh.showMessage("Refreshing the current MAC.");
                                        refreshMAc();
                                    }, 1000));
                        }).start();

                    } else {
                        exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + selectedDevice + " down");
                        command = "bootkali macchanger_custom " + macsArray + " " + selectedDevice;
                        exe.RunAsRootWithException(command);
                        // macResult.setText();

                        new android.os.Handler().postDelayed(
                                () -> {
                                    exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + selectedDevice + " up");
                                    nh.showMessage("Refreshing the current MAC.");
                                    refreshMAc();
                                }, 500);

                    }
                }
                mWakeLock.release();
            }


        });

        return rootView;
    }

    private String[] delFromArray(String[] originalArr, int itemid) {
        List<String> templist = new ArrayList<>();
        for (int i = 0; i < originalArr.length; i++) {
            if (itemid != i) {
                templist.add(originalArr[i]);
            }
        }
        return templist.toArray(new String[templist.size()]);
    }

    private String getMacValues() {
        if (getView() == null) {
            return null;
        }
        View v = getView();
        // edittext for the custom MAC addr
        final EditText mac1 = v.findViewById(R.id.mac1);
        final EditText mac2 = v.findViewById(R.id.mac2);
        final EditText mac3 = v.findViewById(R.id.mac3);
        final EditText mac4 = v.findViewById(R.id.mac4);
        final EditText mac5 = v.findViewById(R.id.mac5);
        final EditText mac6 = v.findViewById(R.id.mac6);

        String delimiter_default = ":";

        return mac1.getText().toString() + delimiter_default +
                mac2.getText().toString() + delimiter_default +
                mac3.getText().toString() + delimiter_default +
                mac4.getText().toString() + delimiter_default +
                mac5.getText().toString() + delimiter_default +
                mac6.getText().toString();
    }

    private void refreshMAc() {
        if (getView() == null) {
            return;
        }
        final Spinner interfaceSpinner = getView().findViewById(R.id.interface_opts);
        final TextView currMac = getView().findViewById(R.id.currMac);
        final Button setMacButton = getView().findViewById(R.id.set_mac_button);
        String selectedInterface = interfaceSpinner.getSelectedItem().toString();
        String cleanInterface = selectedInterface.split(" ")[0];
        getCurrentMac(cleanInterface, currMac, setMacButton);
    }


    private void getCurrentMac(final String theDevice, final TextView currMac, final Button setMacButton) {
        //in the bg

        currMac.setText(String.format("Reading %s", theDevice));

        new Thread(() -> {

            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);


                String fileMac = "/sys/class/net/" + theDevice + "/address";
                final String _res;

                _res = exe.ReadFile_SYNC(fileMac); // get the response

                currMac.post(() -> {
                    if (_res.equals("")) {
                        String notFound = "MAC not found";
                        currMac.setText(notFound);
                        setMacButton.setEnabled(false);
                        setMacButton.setText(String.format("%s not detected", theDevice)); //interface is down| not plugged etc
                    } else {

                        setMacButton.setEnabled(true);
                        currMac.setText(_res); //set the current mac if found

                        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                        String slt = sharedpreferences.getString("macchanger_opts", "");


                        // update button
                        if (slt.equals("Random MAC")) {
                            setMacButton.setText(String.format("Set Random MAC on %s", theDevice));
                        }

                        if (slt.equals("Custom MAC")) {
                            setMacButton.setText(String.format("Set Custom MAC on %s", theDevice));

                        }
                    }

                });

            } catch (final Exception e) {
                currMac.post(() -> currMac.setText(String.format("Error getting %s MAC: %s", theDevice, e)));
            }
        }).start();

    }

    private void resetMac() {
        if (getView() == null) {
            return;
        }
        View v = getView();
        final Spinner interfaceSpinner = v.findViewById(R.id.interface_opts);
        String selectedInterface = interfaceSpinner.getSelectedItem().toString();
        final String cleanInterface = selectedInterface.split(" ")[0];
        String command;
        if (cleanInterface.equals("wlan0")) {
            nh.showMessage("Resetting " + cleanInterface + " MAC");
            if (isOPO()) {
                Log.d("opo_original_mac", sharedpreferences.getString("opo_original_mac", ""));
                command = "settings put global airplane_mode_on 1" +
                        " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true" +
                        " && echo \"" + sharedpreferences.getString("opo_original_mac", "") + "\" > /sys/devices/fb000000.qcom,wcnss-wlan/wcnss_mac_addr" +
                        " && sleep 1" +
                        " && settings put global airplane_mode_on 0" +
                        " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
            } else {
                command = "settings put global airplane_mode_on 1" +
                        " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true" +
                        " && sleep 1" +
                        " && settings put global airplane_mode_on 0" +
                        " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";

            }
            final String finalCommand = command;
            new Thread(() -> {
                exe.RunAsRootWithException(finalCommand);
                interfaceSpinner.post(() -> new android.os.Handler().postDelayed(
                        () -> {
                            nh.showMessage("Refreshing the current MAC.");
                            refreshMAc();
                        }, 1000));
            }).start();
        } else {
            nh.showMessage("Resetting " + cleanInterface + " MAC");
            exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + cleanInterface + " down");

            String resetCmd = "bootkali macchanger_original " + cleanInterface;
            exe.RunAsRootWithException(resetCmd);

            new android.os.Handler().postDelayed(
                    () -> {
                        exe.RunAsRootWithException(nh.whichBusybox() + " ifconfig " + cleanInterface + " up");
                        nh.showMessage("Refreshing the current MAC.");
                        refreshMAc();
                    }, 500);

        }
    }

    private String randomMACAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);
        macAddr[0] = (byte) (macAddr[0] & (byte) 254);
        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {

            if (sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public void saveMacDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Saving mac:");
        builder.setMessage("Not implemented WIP");
        builder.setPositiveButton("OK", (dialog, which) -> {

        });
        builder.show();

    }

    public void loadSavedMacDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Load saved MAC:");
        builder.setMessage("Not implemented WIP");
        builder.setPositiveButton("OK", (dialog, which) -> {

        });
        builder.show();
    }

    private String getDeviceName() {
        return Build.DEVICE;
    }

    private Boolean isOPO() {
        return getDeviceName().equalsIgnoreCase("bacon") ||
                getDeviceName().equalsIgnoreCase("A0001") ||
                getDeviceName().equalsIgnoreCase("one") ||
                getDeviceName().equalsIgnoreCase("OnePlus");
    }

    public Boolean isOPO2() {
        return getDeviceName().equalsIgnoreCase("A2001") ||
                getDeviceName().equalsIgnoreCase("A2003") ||
                getDeviceName().equalsIgnoreCase("A2005") ||
                getDeviceName().equalsIgnoreCase("OnePlus2");
    }

    private Boolean isOldDevice() {
        return getDeviceName().equalsIgnoreCase("flo") ||
                getDeviceName().equalsIgnoreCase("deb") ||
                getDeviceName().equalsIgnoreCase("mako");
    }

    private static void setHostname(String host) {
        final ShellExecuter exe = new ShellExecuter();
        exe.RunAsRootWithException("setprop net.hostname " + host);
    }

    private String getHostname() {
        final ShellExecuter exe = new ShellExecuter();
        return exe.RunAsRootWithException("getprop net.hostname  myhost");
    }
}
