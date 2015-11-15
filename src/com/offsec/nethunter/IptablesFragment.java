package com.offsec.nethunter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

//import android.app.Fragment;


public class IptablesFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    NhUtil nh;
    public IptablesFragment() {

    }
    public static IptablesFragment newInstance(int sectionNumber) {
        IptablesFragment fragment = new IptablesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.iptables, container, false);

        EditText source = (EditText) rootView.findViewById(R.id.source);
        File file = new File(nh.APP_SD_FILES_PATH, "/configs/iptables.conf");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e("Nethunter", "exception", e);
        }
        source.setText(text);

        final Button button = (Button) rootView.findViewById(R.id.update);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {

                    File myFile = new File(nh.APP_SD_FILES_PATH, "configs/iptables.conf");
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    EditText source = (EditText) getActivity().findViewById(R.id.source);
                    myOutWriter.append(source.getText());
                    myOutWriter.close();
                    fOut.close();
                    nh.showMessage("Source updated");
                } catch (Exception e) {
                    nh.showMessage(e.getMessage());
                }



            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isAdded()) {
            nh = new NhUtil();
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.iptables, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.run_iptables:
                runIptables();
                return true;
            case R.id.flush:
                flushIptables();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void runIptables() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali iptables'"};
        exe.RunAsRoot(command);
        nh.showMessage("Iptables started");
    }

    public void flushIptables() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"iptables-flush"}; // still works well
        exe.RunAsRoot(command);
        nh.showMessage("Iptables flushed");
    }
}