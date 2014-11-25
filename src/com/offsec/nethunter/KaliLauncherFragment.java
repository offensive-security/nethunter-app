package com.offsec.nethunter;

import android.app.Activity;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class KaliLauncherFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private int ARG_SECTION_NUMBER;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */


    public KaliLauncherFragment(int sectionNumber, String activityName) {
        ARG_SECTION_NUMBER = sectionNumber;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.kali_launcher, container, false);
        addClickListener(R.id.button_start_kali, new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent =
                        new Intent("jackpal.androidterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nbootkali");
                startActivity(intent);
            }
        }, rootView);
        /**
         * Launch Kali menu
         */
        addClickListener(R.id.button_start_kalimenu, new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent =
                        new Intent("jackpal.androidterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nbootkali\nkalimenu");
                startActivity(intent);
            }
        }, rootView);
        /**
         * Shutdown Kali chroot
         */
        addClickListener(R.id.button_stop_kali, new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent =
                        new Intent("jackpal.androidterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nkillkali");
                startActivity(intent);
            }
        }, rootView);
        /**
         * Launch Wifite
         */
        addClickListener(R.id.button_launch_wifite, new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent =
                        new Intent("jackpal.androidterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nstart-wifite");
                startActivity(intent);
            }
        }, rootView);
        /**
         * Start Webserver
         */
        addClickListener(R.id.button_start_web, new View.OnClickListener() {
            public void onClick(View v) {
                String[] command = {"start-web"};
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(command);
            }
        }, rootView);
        /**
         * Stop Webserver
         */
        addClickListener(R.id.button_stop_web, new View.OnClickListener() {
            public void onClick(View v) {
                String[] command = {"stop-web"};
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(command);
            }
        }, rootView);


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(ARG_SECTION_NUMBER);
    }


    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

}
