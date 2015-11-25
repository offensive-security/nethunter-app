package com.offsec.nethunter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.app.Fragment;


public class BadusbFragment extends Fragment {

    private String configFilePath;
    private String sourcePath;
    private static final String ARG_SECTION_NUMBER = "section_number";
    NhPaths nh;
    ShellExecuter exe = new ShellExecuter();
    public BadusbFragment() {

    }

    public static BadusbFragment newInstance(int sectionNumber) {
        BadusbFragment fragment = new BadusbFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.badusb, container, false);
        loadOptions(rootView);
        final Button button = (Button) rootView.findViewById(R.id.updateOptions);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateOptions();
            }
        });
        setHasOptionsMenu(true);
        return rootView;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        nh = new NhPaths();
        if (Build.VERSION.SDK_INT >= 21) {
            configFilePath = "/configs/startbadusb-lollipop.sh";
        } else {
            configFilePath = "/configs/startbadusb-kitkat.sh";
        }
        sourcePath = nh.APP_SD_FILES_PATH + configFilePath;
    }

    public void onResume() {
        super.onResume();
        if(getView() != null){
            loadOptions(getView().getRootView());
        }
    }

    private void loadOptions(View rootView) {
        final EditText ifc = (EditText) rootView.findViewById(R.id.ifc);
        new Thread(new Runnable() {
            public void run() {
                final String text = exe.ReadFile_SYNC(sourcePath);
                ifc.post(new Runnable() {
                    @Override
                    public void run() {
                        String regExpatInterface = "^INTERFACE=(.*)$";
                        Pattern pattern = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            String ifcValue = matcher.group(1);
                            ifc.setText(ifcValue);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.badusb, menu);
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
                i.putExtra("path", sourcePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void updateOptions() {
        String source = exe.ReadFile_SYNC(sourcePath);
        EditText ifc = (EditText) getActivity().findViewById(R.id.ifc);
        source = source.replaceAll("(?m)^INTERFACE=(.*)$", "INTERFACE=" + ifc.getText().toString());
        ContentResolver resolver = this.getContext().getContentResolver();
        File file = new File(sourcePath);
        try {
            OutputStream OS = resolver.openOutputStream(Uri.fromFile(file));
            OS.write(source.getBytes());
            OS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = "start-badusb-lollipop &> "+ nh.APP_SD_FILES_PATH +"/badusb.log &";
        } else {
            command[0] = "start-badusb-kitkat &> "+ nh.APP_SD_FILES_PATH +"/badusb.log &";
        }
        exe.RunAsRoot(command);
        nh.showMessage("BadUSB attack started!");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= 21) {
            command[0] = "stop-badusb-lollipop";
        } else {
            command[0] = "stop-badusb-kitkat";
        }
        exe.RunAsRoot(command);
        nh.showMessage("BadUSB attack stopped!");
    }
}