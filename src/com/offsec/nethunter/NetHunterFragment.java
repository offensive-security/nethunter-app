package com.offsec.nethunter;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.ShellExecuter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetHunterFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String IP_REGEX = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
    private static final Pattern IP_REGEX_PATTERN = Pattern.compile(IP_REGEX);
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
        TextView ip = (TextView) rootView.findViewById(R.id.editText2);
        ip.setFocusable(false);
        addClickListener(R.id.button1, new View.OnClickListener() {
            public void onClick(View v) {
                getExternalIp();
            }
        }, rootView);
        getInterfaces(rootView);

        return rootView;
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

    private void getExternalIp() {

        final TextView ip = (TextView) getActivity().findViewById(R.id.editText2);
        ip.setText("Please wait...");

        new Thread(new Runnable() {
            StringBuilder result = new StringBuilder();

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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        ip.setText(done);
                    }
                });
            }
        }).start();
        // CHECK FOR ROOT ACCESS

    }

    private void getInterfaces(final View rootView) {
        // 1 thread, 2 commands
        final TextView netIfaces = (TextView) rootView.findViewById(R.id.editText1); // NET IFACES
        final TextView hidIfaces = (TextView) rootView.findViewById(R.id.editText3); // HID IFACES
        final ListView netList = (ListView) rootView.findViewById(R.id.listViewNet);
        final ListView hidList = (ListView) rootView.findViewById(R.id.listViewHid);
        // Dont move this inside the thread. (Will throw a null pointer.)
        netIfaces.setText("Detecting Network interfaces...");
        hidIfaces.setText("Detecting HID interfaces...");

        new Thread(new Runnable() {
            public void run() {
                    ShellExecuter exe = new ShellExecuter();
                    String commandNET[] = {"sh", "-c", "ip -o addr show | awk '/inet/ {print $2, $3, $4}'"};
                    String commandHID[] = {"sh", "-c", "ls /dev/hidg*"};

                    final String outputNET = exe.Executer(commandNET);
                    final String outputHID = exe.Executer(commandHID);

                    final String[] netArray = outputNET.split("\n");
                    final String[] hidArray = outputHID.split("\n");
                    netIfaces.post(new Runnable() {
                        @Override
                        public void run() {
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
                                netList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                        Log.d("CLCIKEDD", netList.getItemAtPosition(position).toString());
                                        String itemData = netList.getItemAtPosition(position).toString();
                                        String _itemData = itemData.split("\\s+")[2];
                                        doCopy(_itemData);
                                        return false;
                                    }
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
                                        Log.d("CLCIKEDD", hidList.getItemAtPosition(position).toString());
                                        String itemData = hidList.getItemAtPosition(position).toString();
                                        doCopy(itemData);
                                        return false;
                                    }
                                });
                            }
                        }
                    });
                }
        }).start();
    }
    private void fixListHeight(ListView theListView, ArrayAdapter theAdapter){
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
}