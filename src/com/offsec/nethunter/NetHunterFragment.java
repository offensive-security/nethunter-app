package com.offsec.nethunter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.Fragment;

public class NetHunterFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private static NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String IP_REGEX = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
    private static final Pattern IP_REGEX_PATTERN = Pattern.compile(IP_REGEX);
    Switch HIDSwitch;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */


    public NetHunterFragment() {

    }

    public static NetHunterFragment newInstance(int sectionNumber) {
        NetHunterFragment fragment = new NetHunterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.nethunter, container, false);
        TextView ip = rootView.findViewById(R.id.editText2);
        ip.setFocusable(false);
        addClickListener(v -> getExternalIp(), rootView);
        getInterfaces(rootView);

        return rootView;
    }

    private void addClickListener(View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(R.id.button1).setOnClickListener(onClickListener);
    }

    private void getExternalIp() {

        final TextView ip = getActivity().findViewById(R.id.editText2);
        ip.setText("Please wait...");

        new Thread(new Runnable() {
            final StringBuilder result = new StringBuilder();

            public void run() {

                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    URLConnection urlcon = new URL("https://api.ipify.org").openConnection();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                } catch (Exception e) {
                    result.append("Check connection!");
                }
                final String done;
                Matcher p = IP_REGEX_PATTERN.matcher(result.toString());
                if (p.matches() || result.toString().equals("Check connection!")) {
                    done = result.toString();
                } else {
                    done = "Invalid IP!";
                }
                getActivity().runOnUiThread(() -> ip.setText(done));
            }
        }).start();
        // CHECK FOR ROOT ACCESS

    }

    private void getInterfaces(final View rootView) {

        nh = new NhPaths();

        final boolean installed = appInstalledOrNot("com.offsec.nhterm");


        // 1 thread, 2 commands
        final TextView netIfaces = rootView.findViewById(R.id.editTextNET); // NET IFACES
        final ListView netList = rootView.findViewById(R.id.listViewNet);

        final TextView hidIfaces = rootView.findViewById(R.id.editTextHID); // HID IFACES
        final ListView hidList = rootView.findViewById(R.id.listViewHid);

        final TextView busyboxIfaces = rootView.findViewById(R.id.editTextBUSYBOX); // BUSYBOX IFACES
        final ListView busyboxList = rootView.findViewById(R.id.listViewBusybox);

        final TextView kernelverIfaces = rootView.findViewById(R.id.editTextKERNELVER); // BUSYBOX IFACES
        final ListView kernelverList = rootView.findViewById(R.id.listViewKERNELVER);

        final TextView terminalIfaces = rootView.findViewById(R.id.editTextNHTerminal); // BUSYBOX IFACES
        final ListView terminalList = rootView.findViewById(R.id.listViewNHTerminal);

        // Dont move this inside the thread. (Will throw a null pointer.)
        netIfaces.setText("Detecting Network interfaces...");
        hidIfaces.setText("Detecting HID interfaces...");
        busyboxIfaces.setText("Detecting Busybox version...");
        kernelverIfaces.setText("Detecting Kernel version...");
        terminalIfaces.setText("Detecting Nethunter terminal...");

        new Thread(() -> {

            String busybox_ver = nh.whichBusybox();

            ShellExecuter exe = new ShellExecuter();
            String commandNET[] = {"sh", "-c", "ip -o addr show | busybox awk '/inet/ {print $2, $3, $4}'"};
            String commandHID[] = {"sh", "-c", "ls /dev/hidg*"};
            String commandBUSYBOX[] = {"sh", "-c", busybox_ver + " | " + busybox_ver + " head -1 | " + busybox_ver + " awk '{print $2}'"};
            String commandKERNELVER[] = {"sh", "-c", "cat /proc/version"};

            final String outputNET = exe.Executer(commandNET);
            final String outputHID = exe.Executer(commandHID);
            final String outputBUSYBOX = exe.Executer(commandBUSYBOX);
            final String outputKERNELVER = exe.Executer(commandKERNELVER);

            final String[] netArray = outputNET.split("\n");
            final String[] hidArray = outputHID.split("\n");
            final String[] busyboxArray = outputBUSYBOX.split("\n");
            final String[] kernelverArray = outputKERNELVER.split("\n");

            netIfaces.post(() -> {
                if (outputNET.equals("")) {
                    netIfaces.setVisibility(View.VISIBLE);
                    netList.setVisibility(View.GONE);
                    netIfaces.setText("No network interfaces detected");
                    netIfaces.setFocusable(false);
                } else {
                    netIfaces.setVisibility(View.GONE);
                    netList.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> aaNET = new ArrayAdapter<>(getContext(), R.layout.nethunter_item, netArray);
                    netList.setAdapter(aaNET);
                    fixListHeight(netList, aaNET);
                    netList.setOnItemLongClickListener((parent, view, position, id) -> {
                        Log.d("CLICKED", netList.getItemAtPosition(position).toString());
                        String itemData = netList.getItemAtPosition(position).toString();
                        String _itemData = itemData.split("\\s+")[2];
                        doCopy(_itemData);
                        return false;
                    });
                }
                if (outputHID.equals("")) {
                    hidIfaces.setVisibility(View.VISIBLE);
                    hidList.setVisibility(View.GONE);
                    hidIfaces.setText("No HID interfaces detected");
                    hidIfaces.setFocusable(false);
                } else {
                    hidIfaces.setVisibility(View.GONE);
                    hidList.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> aaHID = new ArrayAdapter<>(getContext(), R.layout.nethunter_item, hidArray);
                    hidList.setAdapter(aaHID);
                    fixListHeight(hidList, aaHID);
                    hidList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("CLCIKED", hidList.getItemAtPosition(position).toString());
                            String itemData = hidList.getItemAtPosition(position).toString();
                            doCopy(itemData);
                            return false;
                        }
                    });
                }
                if (outputBUSYBOX.equals("")) {
                    busyboxIfaces.setVisibility(View.VISIBLE);
                    busyboxList.setVisibility(View.GONE);
                    busyboxIfaces.setText("Busybox not detected!");
                    busyboxIfaces.setFocusable(false);
                } else {
                    busyboxIfaces.setVisibility(View.GONE);
                    busyboxList.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> aaBUSYBOX = new ArrayAdapter<>(getContext(), R.layout.nethunter_item, busyboxArray);
                    busyboxList.setAdapter(aaBUSYBOX);
                    fixListHeight(busyboxList, aaBUSYBOX);
                    busyboxList.setOnItemLongClickListener((parent, view, position, id) -> {
                        Log.d("CLICKED", busyboxList.getItemAtPosition(position).toString());
                        String itemData = busyboxList.getItemAtPosition(position).toString();
                        doCopy(itemData);
                        return false;
                    });
                }
                if (!installed) {
                    // Installed, make note!
                    terminalIfaces.setVisibility(View.VISIBLE);
                    terminalList.setVisibility(View.GONE);
                    terminalIfaces.setText("Nethunter Terminal is NOT installed!");
                    terminalIfaces.setFocusable(false);
                } else {
                    // Not installed, make note!
                    terminalIfaces.setVisibility(View.VISIBLE);
                    terminalList.setVisibility(View.GONE);
                    terminalIfaces.setText("Nethunter Terminal is installed");
                    terminalIfaces.setFocusable(false);
                }

                if (outputKERNELVER.equals("")) {
                    kernelverIfaces.setVisibility(View.VISIBLE);
                    kernelverList.setVisibility(View.GONE);
                    kernelverIfaces.setText("Could not find kernel version!");
                    kernelverIfaces.setFocusable(false);
                } else {
                    kernelverIfaces.setVisibility(View.GONE);
                    kernelverList.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> aaKERNELVER = new ArrayAdapter<>(getContext(), R.layout.nethunter_item, kernelverArray);
                    kernelverList.setAdapter(aaKERNELVER);
                    kernelverList.setOnItemLongClickListener((parent, view, position, id) -> {
                        Log.d("CLICKED", kernelverList.getItemAtPosition(position).toString());
                        String itemData = kernelverList.getItemAtPosition(position).toString();
                        doCopy(itemData);
                        return false;
                    });
                }
            });
        }).start();
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void fixListHeight(ListView theListView, ArrayAdapter theAdapter) {
        int totalHeight = 0;
        for (int i = 0; i < theAdapter.getCount(); i++) {
            View listItem = theAdapter.getView(i, null, theListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = theListView.getLayoutParams();
        params.height = totalHeight + (theListView.getDividerHeight() * (theAdapter.getCount() - 1));
        theListView.setLayoutParams(params);
        theListView.requestLayout();
    }

    // Now we can copy and address from networks!!!!!! Surprise! ;)
    private void doCopy(String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("WordKeeper", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied: " + text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error copying: " + text, Toast.LENGTH_SHORT).show();
        }
    }
    private String getDeviceName() {
        return Build.DEVICE;
    }

    public Boolean isOPO5() {
        return getDeviceName().equalsIgnoreCase("A5000") ||
                getDeviceName().equalsIgnoreCase("A5010") ||
                getDeviceName().equalsIgnoreCase("OnePlus5") ||
                getDeviceName().equalsIgnoreCase("OnePlus5T");
    }
}
