package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;
import com.offsec.nethunter.utils.ShellExecuter;
import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.CustomCommand;
import com.offsec.nethunter.utils.NhPaths;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
/**
 * Created by nik on 20/02/17.
 */




    public class DeAuthFragment  extends Fragment {
    private final ShellExecuter exe = new ShellExecuter();
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
        final Button scan = (Button) rootView.findViewById(R.id.scan_networks);
        final EditText wlan = (EditText) rootView.findViewById(R.id.wlan_interface);
        final EditText term = (EditText) rootView.findViewById(R.id.TerminalOutputDeAuth);
        final Button start = (Button) rootView.findViewById(R.id.StartDeAuth) ;
        final EditText pkt = (EditText) rootView.findViewById(R.id.time);
        final EditText bssid = (EditText) rootView.findViewById(R.id.bssid);
        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                new BootKali("airmon-ng start " + wlan.getText());
                try {
                    Thread.sleep(1000);
                    new BootKali("ifconfig " + wlan.getText() + " up").run_bg();
                    Thread.sleep(1000);
                    intentClickListener_NH("aireplay-ng --deauth " + pkt.getText() + " -a " + bssid.getText() + " " + wlan.getText());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            });
        scan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

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
                    String output = exe.RunAsRootOutput("cat /sdcard/nh_files/deauth/output.txt").replace("Cell ","\n");
                    term.setText(output);
                } catch (Exception e) {
                    e.printStackTrace();
                    term.setText(e.toString());
                }


            }
        });

        return rootView;
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


}
