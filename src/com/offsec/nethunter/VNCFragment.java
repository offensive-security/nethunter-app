package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.pm.PackageManager
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

public class VNCFragment extends Fragment {

    private static final String TAG = "VNCFragment";
    private String xwidth;
    private String xheight;
    private String localhostonly = "";
    private boolean isbVNCinstalled = false;
    NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public VNCFragment() {
    }

    public static VNCFragment newInstance(int sectionNumber) {
        VNCFragment fragment = new VNCFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.vnc_setup, container, false);
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Context mContext = getActivity().getApplicationContext();

        // Get screen size to pass to VNC
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screen_height = displaymetrics.heightPixels;
        final int screen_width = displaymetrics.widthPixels;

        // Because height and width changes on screen rotation, use the largest as width
        if (screen_height > screen_width) {
            xwidth = Integer.toString(screen_height);
            xheight = Integer.toString(screen_width);
        } else {
            xwidth = Integer.toString(screen_width);
            xheight = Integer.toString(screen_height);
        }
        Button SetupVNCButton = (Button) rootView.findViewById(R.id.set_up_vnc);
        Button StartVNCButton = (Button) rootView.findViewById(R.id.start_vnc);
        Button StopVNCButton = (Button) rootView.findViewById(R.id.stop_vnc);
        Button OpenVNCButton = (Button) rootView.findViewById(R.id.vncClientStart);

        String[] resolutions = new String[]{"24-bit color", "256 colors", "64 colors"};

        Spinner resolution_spinner = (Spinner) rootView.findViewById(R.id.resolution_spinner);
        resolution_spinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, resolutions));

        // Checkbox for localhost only
        final CheckBox localhostCheckBox = (CheckBox) rootView.findViewById(R.id.vnc_checkBox);
        localhostCheckBox.setChecked(true);
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (localhostCheckBox.isChecked()) {
                    localhostonly = "-localhost ";
                } else {
                    localhostonly = "";
                }
            }
        };
        localhostCheckBox.setOnClickListener(checkBoxListener);

        addClickListener(SetupVNCButton, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener_NH("vncpasswd && exit"); // since is a kali command we can send it as is
            }
        });
        addClickListener(StartVNCButton, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener_NH("vncserver :1 " + localhostonly + "-geometry " + xwidth + "x" + xheight + " && echo \"Closing terminal in 5 secs\" && sleep 5 && exit"); // since is a kali command we can send it as is
                Log.d(TAG, localhostonly);
            }
        });
        addClickListener(StopVNCButton, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener_NH("vncserver -kill :1 && echo \"Closing terminal in 5 secs\" && sleep 5 && exit"); // since is a kali command we can send it as is
            }
        });
        addClickListener(OpenVNCButton, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener_VNC(); // since is a kali command we can send it as is
            }
        });

	check_bVNC();
        return rootView;
    }


    private void addClickListener(Button _button, View.OnClickListener onClickListener) {
        _button.setOnClickListener(onClickListener);
    }

    private void intentClickListener_VNC() {
        try {
            if (getView() == null) {
                return;
            }

            if (!isbVNCinstalled) {
                Toast.makeText(getActivity().getApplicationContext(), "bVNC app not found!", Toast.LENGTH_LONG).show();
                return;
            }
            String _R_IP = ((EditText) getView().findViewById(R.id.vnc_R_IP)).getText().toString().replaceAll(" ", "");
            String _R_PORT = ((EditText) getView().findViewById(R.id.vnc_R_PORT)).getText().toString().replaceAll(" ", "");
            String _PASSWD = ((EditText) getView().findViewById(R.id.vnc_PASSWD)).getText().toString();
            String _NICK = ((EditText) getView().findViewById(R.id.vnc_CONN_NICK)).getText().toString().replaceAll(" ","");
            String _USER = ((EditText) getView().findViewById(R.id.vnc_USER)).getText().toString();
            int _RESOLUTION = ((Spinner) getView().findViewById(R.id.resolution_spinner)).getSelectedItemPosition();
            String[] resolutions_bVNC = new String[]{"C24bit", "C256", "C64"};
            if (_R_IP.equals("") || _R_PORT.equals("") || _NICK.equals("") || _PASSWD.equals("")){
                Toast.makeText(getActivity().getApplicationContext(), "Make sure ip,port,nickname & password are not empty!", Toast.LENGTH_LONG).show();
            }

            else {
                try {
                    ShellExecuter exe = new ShellExecuter();
                    String command;
                    if (_USER.equals("")) _USER = "kali";
                    command = "bootkali vnc start " + _NICK + " " + _R_IP + " " + _R_PORT + " " + _PASSWD + " " + _USER + " " + resolutions_bVNC[_RESOLUTION];
                    exe.RunAsRootOutput(command);
                    //Intent intent = new Intent("com.iiordanov.freebVNC/com.iiordanov.bVNC.bVNC");
                    //startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), e.toString() , Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            Log.d("errorLaunching", e.toString());
            Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
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
    private void check_bVNC() {
        new Thread(new Runnable() {
            public void run() {
                ShellExecuter exe_check = new ShellExecuter();
                if (exe_check.RunAsRootOutput("pm list packages | grep com.iiordanov.freebVNC").equals("")) {
                    isbVNCinstalled = false;
                } else isbVNCinstalled = true;
            }
        }).start();
    }
}
