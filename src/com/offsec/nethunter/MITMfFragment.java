package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.offsec.nethunter.utils.NhPaths;

public class MITMfFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences sharedpreferences;
    View.OnClickListener checkBoxListener;;

    // All MITMf command settings
    String M_Interface;
    String M_JSKeyLogger;
    String M_FerretNG;
    String M_BrowserProfiler;
    String M_FilePWN;
    String M_BeeF;
    String M_SMB;
    String M_SSLStrip;
    String M_App_Poison;
    String M_UpsideDown;
    String M_ScreenShotter;
    String M_ScreenInterval;
    EditText M_ScreenIntervalTime;


    NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public MITMfFragment() {
    }

    public static MITMfFragment newInstance(int sectionNumber) {
        MITMfFragment fragment = new MITMfFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.mitmf, container, false);
        TabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerMITMF);
        mViewPager.setAdapter(TabsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
            }
        });
        setHasOptionsMenu(true);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        return rootView;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    //public static class TabsPagerAdapter extends FragmentPagerAdapter {
    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {


        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MITMfGeneral();
                case 1:
                    return new MITMfResponder();
                case 2:
                    return new MITMfInject();
                default:
                    return new MITMfInject();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "General Settings";
                case 1:
                    return "Responder Settings";
                case 2:
                    return "Inject Settings";
                default:
                    return "MITMf General";
            }
        }
    }

    public static class MITMfGeneral extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_general, container, false);

            // Optional Presets Spinner
            Spinner typeSpinner = (Spinner) rootView.findViewById(R.id.mitmf_interface);
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.mitmf_interface_array, android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(typeAdapter);
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String selectedItemText = parent.getItemAtPosition(pos).toString();
                    Log.d("Slected: ", selectedItemText);
                    switch (pos) {
                        case 0:
                            // Blank Interface
                            break;
                        case 1:
                            // Interface: wlan0
                            M_Interface = "-i wlan0";
                            break;
                        case 2:
                            // Interface: wlan1
                            M_Interface = "-i wlan1";
                            break;
                        case 3:
                            // Interface: eth0
                            M_Interface = "-i eth0";
                            break;
                        case 4:
                            // Interface: rndis0
                            M_Interface = "-i rndis0";
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //Another interface callback
                }
            });

            // Checkbox for JSKeyLogger Checkbox
            final CheckBox jskeylogCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_jskey);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(jskeylogCheckbox.isChecked()) {
                        M_JSKeyLogger = "--jskeylogger ";
                    }else{
                        M_JSKeyLogger = "";
                    }
                }
            };
            jskeylogCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for FerretNG Checkbox
            final CheckBox ferretNGCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_ferretng);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ferretNGCheckbox.isChecked()) {
                        M_FerretNG = "--ferretng ";
                    }else{
                        M_FerretNG = "";
                    }
                }
            };
            ferretNGCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for BrowserProfiler Checkbox
            final CheckBox BrowserProfilerCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_browserprofile);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(BrowserProfilerCheckbox.isChecked()) {
                        M_BrowserProfiler = "--browserprofiler ";
                    }else{
                        M_BrowserProfiler = "";
                    }
                }
            };
            BrowserProfilerCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for FilePWN Checkbox
            final CheckBox FilePWNCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_filepwn);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(FilePWNCheckbox.isChecked()) {
                        M_FilePWN = "--filepwn ";
                    }else{
                        M_FilePWN = "";
                    }
                }
            };
            FilePWNCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for BeeF AutoRUN Checkbox
            final CheckBox BeeFAutoCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_beef);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(BeeFAutoCheckbox.isChecked()) {
                        M_BeeF = "--beefauto ";
                    }else{
                        M_BeeF = "";
                    }
                }
            };
            BeeFAutoCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for SMB Checkbox
            final CheckBox SMBCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_smb);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SMBCheckbox.isChecked()) {
                        M_SMB = "--smbauth  ";
                    }else{
                        M_SMB = "";
                    }
                }
            };
            SMBCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for SSLStrip
            final CheckBox SSLStripCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_sslstrip);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SSLStripCheckbox.isChecked()) {
                        M_SSLStrip = "--hsts  ";
                    }else{
                        M_SSLStrip = "";
                    }
                }
            };
            SSLStripCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for App Cache Poison
            final CheckBox APPCachePoisonCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_app_poison);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(APPCachePoisonCheckbox.isChecked()) {
                        M_App_Poison = "--appoison  ";
                    }else{
                        M_App_Poison = "";
                    }
                }
            };
            APPCachePoisonCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for UpsideDownInternet
            final CheckBox UpsideDownCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_upsidedown);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(UpsideDownCheckbox.isChecked()) {
                        M_UpsideDown = "--upsidedownternet  ";
                    }else{
                        M_UpsideDown = "";
                    }
                }
            };
            UpsideDownCheckbox.setOnClickListener(checkBoxListener);

            // ScreenShotter Interval Time
            M_ScreenIntervalTime = (EditText) rootView.findViewById(R.id.mitmf_screen_interval);
            M_ScreenIntervalTime.setText("10");
            M_ScreenIntervalTime.setEnabled(false);

            // Checkbox for ScreenShotter Interval
            final CheckBox ScreenShotterIntCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_screenhot_int_enable);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ScreenShotterIntCheckbox.isChecked()) {
                        M_ScreenInterval = "--interval " + M_ScreenIntervalTime.getText(); // Need to do this better, only updates when checkbox selected
                    }else{
                        M_ScreenInterval = "";
                    }
                }
            };
            ScreenShotterIntCheckbox.setEnabled(false);
            ScreenShotterIntCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for ScreenShotter
            final CheckBox ScreenShotterCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_screenshotter);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ScreenShotterCheckbox.isChecked()) {
                        M_ScreenShotter = "--screen  ";
                        ScreenShotterIntCheckbox.setEnabled(true);
                        M_ScreenIntervalTime.setFocusable(true);
                        M_ScreenIntervalTime.setEnabled(true);
                    }else{
                        M_ScreenShotter = "";
                        M_ScreenInterval = "";
                        ScreenShotterIntCheckbox.setEnabled(false);
                        M_ScreenIntervalTime.setFocusable(false);
                        M_ScreenIntervalTime.setEnabled(false);
                        M_ScreenIntervalTime.setKeyListener(null);
                        M_ScreenIntervalTime.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            };
            ScreenShotterCheckbox.setOnClickListener(checkBoxListener);


            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class MITMfInject extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_inject, container, false);
            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class MITMfResponder extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_responder, container, false);
            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }
}