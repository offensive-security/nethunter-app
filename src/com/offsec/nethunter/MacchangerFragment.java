package com.offsec.nethunter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class MacchangerFragment extends Fragment {


    SharedPreferences sharedpreferences;



    private static final String ARG_SECTION_NUMBER = "section_number";
    private String fileDir;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isAdded()) {
            fileDir = getActivity().getFilesDir().toString() + "/scripts";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.macchanger, container, false);
        // get views
        final Spinner interfaceSpinner = (Spinner) rootView.findViewById(R.id.interface_opts);
        final Spinner macModeSpinner = (Spinner) rootView.findViewById(R.id.macchanger_opts);
        final Button setMacButton = (Button) rootView.findViewById(R.id.set_mac_button);
        final View mac_layout = rootView.findViewById(R.id.mac_lay1);

        // edittext for the custom MAC addr
        final EditText mac1 = (EditText) rootView.findViewById(R.id.mac1);
        final EditText mac2 = (EditText) rootView.findViewById(R.id.mac2);
        final EditText mac3 = (EditText) rootView.findViewById(R.id.mac3);
        final EditText mac4 = (EditText) rootView.findViewById(R.id.mac4);
        final EditText mac5 = (EditText) rootView.findViewById(R.id.mac5);
        final EditText mac6 = (EditText) rootView.findViewById(R.id.mac6);

        final TextView macResult = (TextView) rootView.findViewById(R.id.macResult);

        final TextView currMac = (TextView) rootView.findViewById(R.id.currMac);
        final ImageButton reloadMAC = (ImageButton) rootView.findViewById(R.id.reloadMAC);
        // index for the arrays
        int current_interface = -1;
        int current_mode = -1;

        // app general sprefs
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

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
                    setMacButton.setText("Set Random MAC on " + cleanInterface);
                }
                if (macModeSpinner.getSelectedItem().toString().equals("Custom MAC")) {
                    setMacButton.setText("Set Custom MAC on " + cleanInterface);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }


        });

        reloadMAC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedInterface = interfaceSpinner.getSelectedItem().toString();
                String cleanInterface = selectedInterface.split(" ")[0];
                getCurrentMac(cleanInterface, currMac, setMacButton);
            }
        });

        macModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                // update sharedpreferences
                String items = macModeSpinner.getSelectedItem().toString();
                Editor editor = sharedpreferences.edit();
                editor.putString("macchanger_opts", items);
                editor.apply();

                // update button
                if (items.equals(getResources().getString(R.string.randomMAC))) {
                    setMacButton.setText("Set Random MAC on " + interfaceSpinner.getSelectedItem().toString().split(" ")[0]);
                    mac_layout.setVisibility(View.GONE); // hide the linear layout
                    macResult.setText("");
                }

                if (items.equals(getResources().getString(R.string.customMAC))) {
                    setMacButton.setText("Set Custom MAC on " + interfaceSpinner.getSelectedItem().toString().split(" ")[0]);
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
                // Usage: bootkali macchanger <mac address> <interface> || Random: bootkali macchanger random <interface>
                String command;
                final String selectedDevice = interfaceSpinner.getSelectedItem().toString().split(" ")[0];
                final String[] macsArray = getMacValues();
                final ShellExecuter exe = new ShellExecuter();
                if (macModeSpinner.getSelectedItem().toString().equals("Random MAC")) {

                    if (selectedDevice.equals("wlan0")) {
                        macResult.setText(getResources().getString(R.string.wlanNote));
                    } else {
                        exe.Executer("su -c 'busybox ifconfig " + selectedDevice + " down'");

                        command = "su -c '" + fileDir + "/bootkali macchanger random '" + selectedDevice;
                        macResult.setText(exe.Executer(command));

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        exe.Executer("su -c 'busybox ifconfig " + selectedDevice + " up'");
                                        showMessage("Refresh the current MAC.");
                                    }
                                }, 500);
                    }

                }

                if (macModeSpinner.getSelectedItem().toString().equals("Custom MAC")) {

                    if (selectedDevice.equals("wlan0")) {
                        if (getDeviceName().equals("A0001")) {
                            macResult.setText("Spoofing the wlan0 MAC in your device is not supported yet.");
                            return;
                        }
                        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                        if (wifi.isWifiEnabled()) {
                            wifi.setWifiEnabled(false);
                        }
                        if (macsArray[0].length() != 17) {
                            macResult.setText("MAC incomplete, review it.");
                            return;
                        }

                        command = "echo -ne \"" + macsArray[1] + "\" > /persist/wifi/.macaddr && chmod 644 /persist/wifi/.macaddr"; // pipe the new mac to a file and chmod it.


                        exe.RunAsRootOutput(command);

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                                        wifi.setWifiEnabled(true);
                                        macResult.setText("MAC changed on " + selectedDevice + " to " + macsArray[0] + "\n\n You can refresh the current MAC once the WIFI conection is restored.");
                                    }
                                }, 2000);

                    } else {


                        exe.Executer("su -c busybox ifconfig " + selectedDevice + " down");

                        command = "su -c '" + fileDir + "/bootkali macchanger_custom " + macsArray[0] + " " + selectedDevice;
                        macResult.setText(exe.RunAsRootOutput(command));

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        exe.Executer("su -c 'busybox ifconfig " + selectedDevice + " up'");
                                        showMessage("Refresh the current MAC");

                                    }
                                }, 500);

                    }
                }
            }

            private String[] getMacValues() {
                String delimiter_nexus = "\\x";
                String delimiter_default = ":";

                String _m1 = mac1.getText().toString();
                String _m2 = mac2.getText().toString();
                String _m3 = mac3.getText().toString();
                String _m4 = mac4.getText().toString();
                String _m5 = mac5.getText().toString();
                String _m6 = mac6.getText().toString();
                // weird "no cat" mac
                String nexusMac = delimiter_nexus +
                        _m1 + delimiter_nexus +
                        _m2 + delimiter_nexus +
                        _m3 + delimiter_nexus +
                        _m4 + delimiter_nexus +
                        _m5 + delimiter_nexus +
                        _m6;
                // beautifyed mac
                String defaultMac = _m1 + delimiter_default +
                        _m2 + delimiter_default +
                        _m3 + delimiter_default +
                        _m4 + delimiter_default +
                        _m5 + delimiter_default +
                        _m6;

                return new String[]{defaultMac, nexusMac};
            }
        });
        if (!getDeviceName().equals("A0001")) {
            doInitialSetup(rootView);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.macchanger, menu);
    }


    public void onPrepareOptionsMenu(Menu menu) {


        getActivity().invalidateOptionsMenu();
    }

    // menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.load_saved_mac:
                loadSavedMacDialog();
                return true;
            case R.id.save_mac:
                saveMacDialog();
                return true;
            case R.id.reset_mac:
                resetMac();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getCurrentMac(final String theDevice, final TextView currMac, final Button setMacButton) {
        //in the bg

        currMac.setText("Reading " + theDevice);

        new Thread(new Runnable() {
            public void run() {

                try {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    final ShellExecuter exe = new ShellExecuter();
                    String command = "cat /sys/class/net/" + theDevice + "/address";
                    final String _res;


                    _res = exe.RunAsRootOutput(command); // get the response

                    currMac.post(new Runnable() {
                        public void run() {
                            if (_res.equals("")) {
                                currMac.setText("MAC not found");
                                setMacButton.setEnabled(false);
                                setMacButton.setText("Device or MAC not detected"); //interface is down
                            } else {

                                setMacButton.setEnabled(true);
                                currMac.setText(_res); //set the current mac if found

                                sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                                String slt = sharedpreferences.getString("macchanger_opts", "");


                                // update button
                                if (slt.equals("Random MAC")) {
                                    setMacButton.setText("Set Random MAC on " + theDevice);
                                }

                                if (slt.equals("Custom MAC")) {
                                    setMacButton.setText("Set Custom MAC on " + theDevice);

                                }
                            }

                        }
                    });

                } catch (final Exception e) {
                    currMac.post(new Runnable() {
                        public void run() {
                            currMac.setText("Error getting " + theDevice + " MAC: " + e);
                        }
                    });
                }
            }
        }).start();

    }

    public void resetMac() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Macchanger Setup:");
        builder.setMessage(R.string.wlan0ResetInfo);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                ShellExecuter exe = new ShellExecuter();
                String command2 = "rm -rf /persist/wifi && reboot";  //reset original device mac && reboot
                exe.RunAsRootOutput(command2);

            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    public void doInitialSetup(final View rootView) {
        new Thread(new Runnable() {

            public void run() {

                final ShellExecuter exe = new ShellExecuter();
                final String command = "if [ -d /persist/wifi/ ];then echo 1; fi"; //check the dir existence
                final String _res = exe.RunAsRootOutput(command);
                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!_res.equals("1")) {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Macchanger Setup:");
                            builder.setMessage("To make this setup you only need the wifi up and connected to a network.\n\nTHE APP WILL DO THE REST\n\nWe need to create a folder and a file to get the service working well, this also saves a copy of the original mac.\n\nLocation:\n/sdcard/kali-nh/device_mac_backup\n\nAfter this, the device will be rebooted.\n\nThe setup is only needed one time");
                            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    makeWifiDir();

                                }

                            });
                            builder.show();

                        }
                    }
                });

            }

        }).start();



    }

    public void makeWifiDir() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog stepsDialog = builder.create();
        stepsDialog.setMessage("Creating needed folders...");
        stepsDialog.show();
        try {

            final ShellExecuter exe = new ShellExecuter();
            stepsDialog.setMessage("CDM: \nmkdir -p /persist/wifi");
            String command2 = "mkdir -p /persist/wifi";  //create a dir
            exe.RunAsRootOutput(command2);
            stepsDialog.setMessage("Directory creation: OK");

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 500 milliseconds later");
                            backupMac(stepsDialog);

                        }
                    }, 1000);

        } catch (final Exception e) {

            stepsDialog.setMessage("Error initial setup in @checkDir: \n\n" + e);

        }

    }

    public void backupMac(final AlertDialog stepsDialog) {

        stepsDialog.setMessage("Saving a copy of the default mac...");
        try {

            final ShellExecuter exe = new ShellExecuter();
            String command = "cat /sys/class/net/wlan0/address > /sdcard/device_mac_backup";
            final String _res;

            _res = exe.RunAsRootOutput(command); // get the response

            stepsDialog.setMessage("Saving MAC : OK\n\nLOCATION: /sdcard/kali-nh/device_mac_backup" + _res);
            //thasts like a timeout
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            stepsDialog.setMessage("All done! REBOOTING DEVICE...");

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            stepsDialog.dismiss();
                                            String command = "reboot";
                                            exe.RunAsRootOutput(command);
                                        }
                                    }, 1000);
                        }
                    }, 1000);

        } catch (final Exception e) {

            stepsDialog.setMessage("Error initial setup in @backUpMac: \n\n" + e);

        }

    }

    public void saveMacDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Saving mac:");
        builder.setMessage("Not implemented WIP");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

    public void loadSavedMacDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Load saved MAC:");
        builder.setMessage("Not implemented WIP");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }

        });
        builder.show();
    }

    public void showMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getActivity(), message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public String getDeviceName() {
        return Build.DEVICE;
    }
}
