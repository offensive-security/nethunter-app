package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

public class VNCFragment extends Fragment {

    private static final String TAG = "VNCFragment";
    private String xwidth;
    private String xheight;
    private String localhostonly = "";

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
        Button SetupVNCButton = rootView.findViewById(R.id.set_up_vnc);
        Button StartVNCButton = rootView.findViewById(R.id.start_vnc);
        Button StopVNCButton = rootView.findViewById(R.id.stop_vnc);
        Button OpenVNCButton = rootView.findViewById(R.id.vncClientStart);

        String[] resolutions = new String[]{"Native", "256 Colors", "64 Colors"};
        Spinner resolution_spinner = rootView.findViewById(R.id.resolution_spinner);
        resolution_spinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, resolutions));

        // Checkbox for localhost only
        final CheckBox localhostCheckBox = rootView.findViewById(R.id.vnc_checkBox);
        localhostCheckBox.setChecked(true);
        View.OnClickListener checkBoxListener = v -> {
            if (localhostCheckBox.isChecked()) {
                localhostonly = "-localhost ";
            } else {
                localhostonly = "";
            }
        };
        localhostCheckBox.setOnClickListener(checkBoxListener);

        addClickListener(SetupVNCButton, v -> {
            intentClickListener_NH("vncpasswd && exit"); // since is a kali command we can send it as is
        });
        addClickListener(StartVNCButton, v -> {
            intentClickListener_NH("vncserver :1 " + localhostonly + "-geometry " + xwidth + "x" + xheight + " && echo \"Closing terminal in 5 secs\" && sleep 5 && exit"); // since is a kali command we can send it as is
            Log.d(TAG, localhostonly);
        });
        addClickListener(StopVNCButton, v -> {
            intentClickListener_NH("vncserver -kill :1 && echo \"Closing terminal in 5 secs\" && sleep 5 && exit"); // since is a kali command we can send it as is
        });
        addClickListener(OpenVNCButton, v -> {
            intentClickListener_VNC(); // since is a kali command we can send it as is
        });


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

            String _R_IP = ((EditText) getView().findViewById(R.id.vnc_R_IP)).getText().toString();
            String _R_PORT = ((EditText) getView().findViewById(R.id.vnc_R_PORT)).getText().toString();
            String _PASSWD = ((EditText) getView().findViewById(R.id.vnc_PASSWD)).getText().toString();
            String _NICK = ((EditText) getView().findViewById(R.id.vnc_CONN_NICK)).getText().toString();
            String _USER = ((EditText) getView().findViewById(R.id.vnc_USER)).getText().toString();
            int _RESOLUTION = ((Spinner) getView().findViewById(R.id.resolution_spinner)).getSelectedItemPosition();
            if (!_R_IP.equals("") && !_R_PORT.equals("") && !_NICK.equals("")) {
                Intent intent = getActivity().getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.offsec.nhvnc");
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.putExtra("com.offsec.nhvnc.EXTRA_CONN_DATA", true);
                intent.putExtra("R_IP", _R_IP);
                intent.putExtra("R_PORT", _R_PORT);
                intent.putExtra("PASSWD", _PASSWD);
                intent.putExtra("NICK", _NICK);
                intent.putExtra("USER", _USER);
                intent.putExtra("COLORMODEL", _RESOLUTION);

                startActivity(intent);
            }

        } catch (Exception e) {
            Log.d("errorLaunching", e.toString());
            Toast.makeText(getActivity().getApplicationContext(), "NetHunter VNC not found!", Toast.LENGTH_SHORT).show();
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
}