package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
/**
 * Created by nik on 20/02/17.
 */




    public class DeAuthFragment  extends Fragment {
    private final ShellExecuter exe = new ShellExecuter();
    private FragmentActivity myContext;

    private NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static DeAuthFragment newInstance(int sectionNumber) {
        DeAuthFragment fragment = new DeAuthFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.deauth, container, false);

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();
        setHasOptionsMenu(true);
        final Button scan = rootView.findViewById(R.id.scan_networks);
        final EditText wlan = rootView.findViewById(R.id.wlan_interface);
        final EditText term = rootView.findViewById(R.id.TerminalOutputDeAuth);
        final Button start = rootView.findViewById(R.id.StartDeAuth);
        final EditText pkt = rootView.findViewById(R.id.time);
        final EditText channel = rootView.findViewById(R.id.channel);
        final CheckBox whitelist = rootView.findViewById(R.id.deauth_whitelist);
        final CheckBox white_me = rootView.findViewById(R.id.deauth_me);
        whitelist.setChecked(false);
        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String whitelist_command;
                new BootKali("ifconfig " + wlan.getText() + " up");
                try {
                    Thread.sleep(1000);
                    new BootKali("airmon-ng start  " + wlan.getText()).run_bg();
                    Thread.sleep(2000);
                    if (whitelist.isChecked()){
                        whitelist_command = "-w /sdcard/nh_files/deauth/whitelist.txt ";
                    }
                    else{
                        whitelist_command = "";
                    }
                    intentClickListener_NH("echo Press Crtl+C to stop! && mdk3 " + wlan.getText() + "mon d " + whitelist_command + "-c " + channel.getText());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            });
        scan.setOnClickListener(v -> {

            /**TODO: create .sh that executes the commands and puts its output in a file and then read the file in the textview 20/02/17*/
            new BootKali("cp /sdcard/nh_files/deauth/scan.sh /root/scan.sh & chmod +x /root/scan.sh").run_bg();
            String cmd = "./root/scan.sh " + wlan.getText() + " | tr -s [:space:] > /sdcard/nh_files/deauth/output.txt";
            try {
                new BootKali("ifconfig " + wlan.getText() + " up").run_bg();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new BootKali(cmd).run_bg();
            try {
                Thread.sleep(5000);
                nh = new NhPaths();
                String output = exe.RunAsRootOutput("cat /sdcard/nh_files/deauth/output.txt").replace("Channel:","\n Channel:");
                term.setText(output);
            } catch (Exception e) {
                e.printStackTrace();
                term.setText(e.toString());
            }


        });
        whitelist.setOnClickListener(v -> {
            if (whitelist.isChecked()){
                white_me.setClickable(true);
                String check_me = exe.RunAsRootOutput("grep -q " + getmac(wlan.getText().toString()) + " \"/sdcard/nh_files/deauth/whitelist.txt\" && echo $?");
                if (check_me.contains("0")){
                    white_me.setChecked(true);
                }
                else{
                    white_me.setChecked(false);
                }
            }
            else{
                white_me.setChecked(false);
                white_me.setClickable(false);
            }
        });
        white_me.setOnClickListener(v -> {
            if (whitelist.isChecked()) {
                if (white_me.isChecked()) {
                    if (wlan.getText().toString() == "wlan0") {
                        exe.RunAsRootOutput("echo '" + getmac(wlan.getText().toString()) + "' >> /sdcard/nh_files/deauth/whitelist.txt");

                    } else {
                        exe.RunAsRootOutput("echo '" + getmac("wlan0") + "' >> /sdcard/nh_files/deauth/whitelist.txt");
                        exe.RunAsRootOutput("echo '" + getmac(wlan.getText().toString()) + "' >> /sdcard/nh_files/deauth/whitelist.txt");
                    }
                } else {
                    if (wlan.getText().toString() == "wlan0") {
                        exe.RunAsRootOutput("sed -i '/" + getmac(wlan.getText().toString()) + "/d' /sdcard/nh_files/deauth/whitelist.txt");
                    } else {
                        exe.RunAsRootOutput("sed -i '/wlan0/d' /sdcard/nh_files/deauth/whitelist.txt");
                        exe.RunAsRootOutput("sed -i '/" + getmac(wlan.getText().toString()) + "/d' /sdcard/nh_files/deauth/whitelist.txt");
                    }
                }
            }
            else{
                white_me.setChecked(false);
            }
        });
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.deauth, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deauth_modify:
                Intent i = new Intent(getActivity(), DeAuthWhitelistActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();

        }
    }

    public String getmac(final String wlan){
        final String mac;
        mac = exe.RunAsRootOutput("cat /sys/class/net/"+ wlan +  "/address");
        return mac;
    }
}
