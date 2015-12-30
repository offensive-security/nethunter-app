package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;

public class MITMfFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences sharedpreferences;
    View.OnClickListener checkBoxListener;;
    // ^^ \\
    // static String CommandComposed = "";
    static ArrayList<String> CommandComposed = new ArrayList<>();

    /* All MITMf General Command Variables */

    String M_Interface; // -i [interface from spinner]
    String M_JSKeyLogger; // --jskeylogger
    String M_FerretNG; // --ferretng
    String M_BrowserProfiler; // --browserprofiler
    String M_FilePWN; // --filepwn
    String M_BeeF; // --beefauto
    String M_SMB; // --smbauth
    String M_SSLStrip; // --hsts
    String M_App_Poison; // --appoison
    String M_UpsideDown;  // --upsidedownternet
    String M_ScreenShotter; // --screen
    String M_ScreenInterval; // --interval [M_ScreenIntervalTime]
    EditText M_ScreenIntervalTime; // Time for Screen interval

    /* All MITMf Responder Command Variables */

    String M_Responder; // --responder
    String M_Responder_Analyze; // --analyze
    String M_Responder_Fingerprint; // --fingerprint
    String M_Responder_Downgrade; // --lm
    String M_Responder_NBTNS; // --nbtns
    String M_Responder_WPAD; // --wpad
    String M_Responder_WRedir; // --wredir

    /* All MITMf Injection Command Variables */

    String M_Injection; // --inject
    String M_Injection_Preserve_Cache; // --preserve-cache
    String M_Injection_Per_Domain; // --per-domain
    String M_Injection_JSURL; // --js-url [M_Injection_JSURL_Text]
    EditText M_Injection_JSURL_Text; // URL STring
    String M_Injection_HTMLURL; // --html-url [M_Injection_HTMLURL_Text]
    EditText M_Injection_HTMLURL_Text; // URL String
    String M_Injection_HTMLPAY; // --html-payload [EditText M_Injection_HTMLPAY_Text]
    EditText M_Injection_HTMLPAY_Text; // HTML String
    String M_Injection_Match; // --match-str [M_Injection_Match_Text]
    EditText M_Injection_Match_Text; // Match String
    String M_Injection_Rate_Limit; // --rate-limit [M_Injection_Rate_Limit_Text]
    EditText M_Injection_Rate_Limit_Text; // Number of seconds
    String M_Injection_Number; // --count-limit
    EditText M_Injection_Number_Text; // Number of seconds
    String M_Injection_Only_IP; // --white-ips [M_Injection_Only_IP_Text]
    EditText M_Injection_Only_IP_Text; // IP
    String M_Injection_Not_IP; // --black-ips [M_Injection_Not_IP_Text]
    EditText M_Injection_Not_IP_Text; // IP

    /* All MITMf Spoof Command Variables */
    String M_Spoofer; // --spoof
    String M_Spoofer_Redirect; // --arp | --icmp | --dhcp | --dns
    String M_Spoofer_ARP_Mode; // --arpmode req | --arpmode rep
    String M_Spoofer_Gateway; // --gateway [M_Spoofer_Gateway_Text]
    EditText M_Spoofer_Gateway_Text; // IP of gateway
    String M_Spoofer_Targets; // --targets [M_Spoofer_Targets_Text]
    EditText M_Spoofer_Targets_Text; // IP of target
    String M_Spoofer_Shellshock; // --shellshock [M_Spoofer_Shellshock_Text]
    EditText M_Spoofer_Shellshock_Text; // Command to run after shellshock

    /* EditText getText toString */
    String HTMLURLtext; // M_Injection_HTMLURL_Text.getText().toString();
    String HTMLPAYtext; //M_Injection_HTMLPAY_Text.getText().toString();
    String SCREENTIMEtext; // M_ScreenIntervalTime.getText().toString();
    String MATCHtext; // M_Injection_Match_Text.getText().toString();
    String RATEtext; // M_Injection_Rate_Limit_Text.getText().toString();
    String NUMtext; // M_Injection_Number_Text.getText().toString();
    String NOTIPtext; // M_Injection_Not_IP_Text.getText().toString();
    String GATEtext; // M_Spoofer_Gateway_Text.getText().toString();
    String TARGETtext; // M_Spoofer_Targets_Text.getText().toString();
    String ONLYIPtext; // M_Injection_Only_IP_Text.getText().toString();
    String SHELLtext; // M_Spoofer_Shellshock_Text.getText().toString();
    String JSURLtext; // M_Injection_JSURL_Text.getText().toString();

    static NhPaths nh;

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
        cleanCmd();
        View rootView = inflater.inflate(R.layout.mitmf, container, false);
        TabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerMITMF);
        mViewPager.setAdapter(TabsPagerAdapter);

        nh = new NhPaths();

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

    /* Start execution menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mitmf, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mitmf_menu_start_service:
                start();
                return true;
            case R.id.mitmf_menu_stop_service:
                stop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void start() {
        intentClickListener_NH("mitmf " + getCmd());
        nh.showMessage("MITMf Started!");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        exe.RunAsRoot(command);
        nh.showMessage("MITMf Stopped!");
    }
    /* Stop execution menu */

    /* Start Tabs */
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
                case 3:
                    return new MITMfSpoof();
                case 4:
                    return new MITMfConfigFragment();
                default:
                    return null;
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Responder Settings";
                case 2:
                    return "Inject Settings";
                case 3:
                    return "Spoof Settings";
                case 4:
                    return "MITMf Configuration";
                default:
                    return "General Settings";
            }
        }
    }
    /* Stop Tabs */

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
            Spinner interfaceSpinner = (Spinner) rootView.findViewById(R.id.mitmf_interface);
            ArrayAdapter<CharSequence> interfaceAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.mitmf_interface_array, android.R.layout.simple_spinner_item);
            interfaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            interfaceSpinner.setAdapter(interfaceAdapter);
            interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String selectedItemText = parent.getItemAtPosition(pos).toString();
                    switch (pos) {
                        case 0:
                            // Interface: wlan0
                            removeFromCmd(M_Interface);
                            M_Interface = " -i wlan0";
                            addToCmd(M_Interface);
                            break;
                        case 1:
                            // Interface: wlan1
                            removeFromCmd(M_Interface);
                            M_Interface = " -i wlan1";
                            addToCmd(M_Interface);
                            break;
                        case 2:
                            // Interface: eth0
                            removeFromCmd(M_Interface);
                            M_Interface = " -i eth0";
                            addToCmd(M_Interface);
                            break;
                        case 3:
                            // Interface: rndis0
                            removeFromCmd(M_Interface);
                            M_Interface = " -i rndis0";
                            addToCmd(M_Interface);
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
                        M_JSKeyLogger = " --jskeylogger";
                        Log.d("MITMf:", M_JSKeyLogger);
                        addToCmd(M_JSKeyLogger);
                    }else{
                        removeFromCmd(M_JSKeyLogger);
                    }
                }
            };
            jskeylogCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for FerretNG Checkbox
            final CheckBox ferretNGCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_ferretng);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ferretNGCheckbox.isChecked()) {
                        M_FerretNG = " --ferretng";
                        addToCmd(M_FerretNG);
                    }else{
                        removeFromCmd(M_FerretNG);
                    }
                }
            };
            ferretNGCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for BrowserProfiler Checkbox
            final CheckBox BrowserProfilerCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_browserprofile);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(BrowserProfilerCheckbox.isChecked()) {
                        M_BrowserProfiler = " --browserprofiler";
                        addToCmd(M_BrowserProfiler);
                    }else{
                        removeFromCmd(M_BrowserProfiler);
                    }
                }
            };
            BrowserProfilerCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for FilePWN Checkbox
            final CheckBox FilePWNCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_filepwn);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(FilePWNCheckbox.isChecked()) {
                        M_FilePWN = " --filepwn";
                        addToCmd(M_FilePWN);
                    }else{
                        removeFromCmd(M_FilePWN);
                    }
                }
            };
            FilePWNCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for BeeF AutoRUN Checkbox
            final CheckBox BeeFAutoCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_beef);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(BeeFAutoCheckbox.isChecked()) {
                        M_BeeF = " --beefauto";
                        addToCmd(M_BeeF);
                    }else{
                        removeFromCmd(M_BeeF);
                    }
                }
            };
            BeeFAutoCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for SMB Checkbox
            final CheckBox SMBCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_smb);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SMBCheckbox.isChecked()) {
                        M_SMB = " --smbauth";
                        addToCmd(M_SMB);
                    }else{
                        removeFromCmd(M_SMB);
                    }
                }
            };
            SMBCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for SSLStrip
            final CheckBox SSLStripCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_sslstrip);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SSLStripCheckbox.isChecked()) {
                        M_SSLStrip = " --hsts";
                        addToCmd(M_SSLStrip);
                    }else{
                        removeFromCmd(M_SSLStrip);
                    }
                }
            };
            SSLStripCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for App Cache Poison
            final CheckBox APPCachePoisonCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_app_poison);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(APPCachePoisonCheckbox.isChecked()) {
                        M_App_Poison = " --appoison";
                        addToCmd(M_App_Poison);
                    }else{
                        removeFromCmd(M_App_Poison);
                    }
                }
            };
            APPCachePoisonCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for UpsideDownInternet
            final CheckBox UpsideDownCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_upsidedown);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(UpsideDownCheckbox.isChecked()) {
                        M_UpsideDown = " --upsidedownternet";
                        addToCmd(M_UpsideDown);
                    }else{
                        removeFromCmd(M_UpsideDown);
                    }
                }
            };
            UpsideDownCheckbox.setOnClickListener(checkBoxListener);
            // ScreenShotter Interval Time
            M_ScreenIntervalTime = (EditText) rootView.findViewById(R.id.mitmf_screen_interval);
            M_ScreenIntervalTime.setText("10");
            M_ScreenIntervalTime.setEnabled(false);
            // Detect changes to TextField
            M_ScreenIntervalTime.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_ScreenInterval + SCREENTIMEtext); // Clear previous command
                    M_ScreenInterval = " --interval "; // Define --interval [num]
                    SCREENTIMEtext = M_ScreenIntervalTime.getText().toString(); // Get [num]
                    addToCmd(M_ScreenInterval + SCREENTIMEtext); // AddToCmd --interval [num]
                }
            });

            // Checkbox for ScreenShotter Interval
            final CheckBox ScreenShotterIntCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_screenhot_int_enable);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ScreenShotterIntCheckbox.isChecked()) {
                        M_ScreenInterval = " --interval "; // Need to do this better, only updates when checkbox selected
                        SCREENTIMEtext = M_ScreenIntervalTime.getText().toString();
                        addToCmd(M_ScreenInterval + SCREENTIMEtext);
                    }else{
                        removeFromCmd(M_ScreenInterval + SCREENTIMEtext);
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
                        M_ScreenShotter = " --screen";
                        addToCmd(M_ScreenShotter);
                        ScreenShotterIntCheckbox.setEnabled(true);
                        ScreenShotterIntCheckbox.setChecked(true);
                        M_ScreenIntervalTime.setFocusable(true);
                        M_ScreenIntervalTime.setEnabled(true);
                    } else {
                        removeFromCmd(M_ScreenShotter);
                        removeFromCmd(M_ScreenInterval + SCREENTIMEtext);
                        ScreenShotterIntCheckbox.setChecked(false);
                        ScreenShotterIntCheckbox.setEnabled(false);
                        M_ScreenIntervalTime.setFocusable(false);
                        M_ScreenIntervalTime.setEnabled(false);
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

            // Checkbox for Injection Preserve Cache
            final CheckBox InjectionPreserveCacheCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_preservecache);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionPreserveCacheCheckbox.isChecked()) {
                        M_Injection_Preserve_Cache = " --preserve-cache";
                        addToCmd(M_Injection_Preserve_Cache);
                    }else{
                        removeFromCmd(M_Injection_Preserve_Cache);
                    }
                }
            };
            InjectionPreserveCacheCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for Injection Once Per Domain
            final CheckBox InjectionPerDomainCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_onceperdomain);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionPerDomainCheckbox.isChecked()) {
                        M_Injection_Per_Domain = " --per-domain";
                        addToCmd(M_Injection_Per_Domain);
                    }else{
                        M_Injection_Per_Domain = "";
                        removeFromCmd(M_Injection_Per_Domain);
                    }
                }
            };
            InjectionPerDomainCheckbox.setOnClickListener(checkBoxListener);

            // Textfield JS URL
            M_Injection_JSURL_Text = (EditText) rootView.findViewById(R.id.mitmf_injectjs_url);
            M_Injection_JSURL_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_JSURL_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_JSURL + JSURLtext);
                    M_Injection_JSURL = " --js-url ";
                    JSURLtext = M_Injection_JSURL_Text.getText().toString();
                    addToCmd(M_Injection_JSURL + JSURLtext);
                }
            });


            // Checkbox for Injection JS URL
            final CheckBox InjectionJSURLCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_injectjs);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionJSURLCheckbox.isChecked()) {
                        M_Injection_JSURL = " --js-url ";
                        M_Injection_JSURL_Text.setFocusable(true);
                        M_Injection_JSURL_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_JSURL + JSURLtext);
                        M_Injection_JSURL_Text.setFocusable(false);
                        M_Injection_JSURL_Text.setEnabled(false);

                    }
                }
            };
            InjectionJSURLCheckbox.setOnClickListener(checkBoxListener);

            // Textfield HTML URL
            M_Injection_HTMLURL_Text = (EditText) rootView.findViewById(R.id.mitmf_injecthtml_url);
            M_Injection_HTMLURL_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_HTMLURL_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_HTMLURL + HTMLURLtext);
                    M_Injection_HTMLURL = " --html-url ";
                    HTMLURLtext = M_Injection_HTMLURL_Text.getText().toString();
                    addToCmd(M_Injection_HTMLURL + HTMLURLtext);
                }
            });

            // Checkbox for Injection HTML URL
            final CheckBox InjectionHTMLURLCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_injecthtml);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionHTMLURLCheckbox.isChecked()) {
                        M_Injection_HTMLURL = " --html-url ";
                        M_Injection_HTMLURL_Text.setFocusable(true);
                        M_Injection_HTMLURL_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_HTMLURL + HTMLURLtext);
                        M_Injection_HTMLURL_Text.setFocusable(false);
                        M_Injection_HTMLURL_Text.setEnabled(false);
                    }
                }
            };
            InjectionHTMLURLCheckbox.setOnClickListener(checkBoxListener);

            // Textfield HTML Payload String
            M_Injection_HTMLPAY_Text = (EditText) rootView.findViewById(R.id.mitmf_injecthtmlpay_text);
            M_Injection_HTMLPAY_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_HTMLPAY_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_HTMLPAY + HTMLPAYtext);
                    M_Injection_HTMLPAY = " --html-payload ";
                    HTMLPAYtext = M_Injection_HTMLPAY_Text.getText().toString();
                    addToCmd(M_Injection_HTMLPAY + HTMLPAYtext);
                }
            });

            // Checkbox for Injection HTML Payload String
            final CheckBox InjectionHTMLPAYCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_injecthtmlpay);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionHTMLPAYCheckbox.isChecked()) {
                        M_Injection_HTMLPAY = " --html-payload ";
                        addToCmd(M_Injection_HTMLPAY + HTMLPAYtext);
                        M_Injection_HTMLPAY_Text.setFocusable(true);
                        M_Injection_HTMLPAY_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_HTMLPAY + HTMLPAYtext);
                        M_Injection_HTMLPAY_Text.setFocusable(false);
                        M_Injection_HTMLPAY_Text.setEnabled(false);

                    }
                }
            };
            InjectionHTMLPAYCheckbox.setOnClickListener(checkBoxListener);

            // Textfield HTML Match String
            M_Injection_Match_Text = (EditText) rootView.findViewById(R.id.mitmf_inject_match_string);
            M_Injection_Match_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_Match_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_Match + MATCHtext);
                    M_Injection_Match = " --match-str ";
                    MATCHtext = M_Injection_Match_Text.getText().toString();
                    addToCmd(M_Injection_Match + MATCHtext);
                }
            });

            // Checkbox for Injection Match HTML
            final CheckBox InjectionMatchCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_match);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionMatchCheckbox.isChecked()) {
                        M_Injection_Match = " --match-str ";
                        M_Injection_Match_Text.setFocusable(true);
                        M_Injection_Match_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_Match + MATCHtext);
                        M_Injection_Match_Text.setFocusable(false);
                        M_Injection_Match_Text.setEnabled(false);

                    }
                }
            };
            InjectionMatchCheckbox.setOnClickListener(checkBoxListener);

            // Textfield for Injection Rate Limit
            M_Injection_Rate_Limit_Text = (EditText) rootView.findViewById(R.id.mitmf_inject_rateseconds);
            M_Injection_Rate_Limit_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_Rate_Limit_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_Rate_Limit + RATEtext);
                    M_Injection_Rate_Limit = " --rate-limit ";
                    RATEtext = M_Injection_Rate_Limit_Text.getText().toString();
                    addToCmd(M_Injection_Rate_Limit + RATEtext);
                }
            });

            // Checkbox for Injection Rate Limit
            final CheckBox InjectionRateLimitCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_ratelimit);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionRateLimitCheckbox.isChecked()) {
                        M_Injection_Rate_Limit = " --rate-limit ";
                        M_Injection_Rate_Limit_Text.setFocusable(true);
                        M_Injection_Rate_Limit_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_Rate_Limit + RATEtext);
                        M_Injection_Rate_Limit_Text.setFocusable(false);
                        M_Injection_Rate_Limit_Text.setEnabled(false);
                    }
                }
            };
            InjectionRateLimitCheckbox.setOnClickListener(checkBoxListener);

            // Textfield for Injection Count Limit
            M_Injection_Number_Text = (EditText) rootView.findViewById(R.id.mitmf_inject_times_text);
            M_Injection_Number_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_Number_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_Number + NUMtext);
                    M_Injection_Number = " --count-limit ";
                    NUMtext = M_Injection_Number_Text.getText().toString();
                    addToCmd(M_Injection_Number + NUMtext);
                }
            });

            // Checkbox for Injection Count Limit
            final CheckBox InjectionNumberCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_times);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionNumberCheckbox.isChecked()) {
                        M_Injection_Number = " --count-limit ";
                        M_Injection_Number_Text.setFocusable(true);
                        M_Injection_Number_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_Number + NUMtext);
                        M_Injection_Number = "";
                        M_Injection_Number_Text.setFocusable(false);
                        M_Injection_Number_Text.setEnabled(false);
                        NUMtext = "";
                    }
                }
            };
            InjectionNumberCheckbox.setOnClickListener(checkBoxListener);

            // Textfield Inject Only IP
            M_Injection_Only_IP_Text = (EditText) rootView.findViewById(R.id.mitmf_inject_ip_text);
            M_Injection_Only_IP_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_Only_IP_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_Only_IP + ONLYIPtext);
                    M_Injection_Only_IP = " --white-ips ";
                    ONLYIPtext = M_Injection_Only_IP_Text.getText().toString();
                    addToCmd(M_Injection_Only_IP + ONLYIPtext);
                }
            });

            // Checkbox for Injection Only Target IP
            final CheckBox InjectionOnlyIPCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_ip);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionOnlyIPCheckbox.isChecked()) {
                        M_Injection_Only_IP = " --white-ips ";
                        M_Injection_Only_IP_Text.setFocusable(true);
                        M_Injection_Only_IP_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_Only_IP + ONLYIPtext);
                        M_Injection_Only_IP_Text.setFocusable(false);
                        M_Injection_Only_IP_Text.setEnabled(false);

                    }
                }
            };
            InjectionOnlyIPCheckbox.setOnClickListener(checkBoxListener);

            // Textfield Inject Not IP
            M_Injection_Not_IP_Text = (EditText) rootView.findViewById(R.id.mitmf_inject_noip_text);
            M_Injection_Not_IP_Text.setEnabled(false);
            // Detect changes to TextField
            M_Injection_Not_IP_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Injection_Not_IP + NOTIPtext);
                    M_Injection_Not_IP = " --black-ips ";
                    NOTIPtext = M_Injection_Not_IP_Text.getText().toString();
                    addToCmd(M_Injection_Not_IP + NOTIPtext);
                }
            });

            // Checkbox for Injection Not IP
            final CheckBox InjectionNotIPCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_inject_noip);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionNotIPCheckbox.isChecked()) {
                        M_Injection_Not_IP = " --black-ips ";
                        M_Injection_Not_IP_Text.setFocusable(true);
                        M_Injection_Not_IP_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Injection_Not_IP + NOTIPtext);
                        M_Injection_Not_IP_Text.setFocusable(false);
                        M_Injection_Not_IP_Text.setEnabled(false);

                    }
                }
            };
            InjectionNotIPCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for Injection
            final CheckBox InjectionCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_enableinject);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(InjectionCheckbox.isChecked()) {
                        M_Injection = " --inject";
                        addToCmd(M_Injection);
                        /* Allow checkboxes to be enabled if Injection checkbox activated */
                        InjectionPreserveCacheCheckbox.setEnabled(true);
                        InjectionPerDomainCheckbox.setEnabled(true);
                        InjectionJSURLCheckbox.setEnabled(true);
                        InjectionHTMLURLCheckbox.setEnabled(true);
                        InjectionHTMLPAYCheckbox.setEnabled(true);
                        InjectionMatchCheckbox.setEnabled(true);
                        InjectionRateLimitCheckbox.setEnabled(true);
                        InjectionNumberCheckbox.setEnabled(true);
                        InjectionOnlyIPCheckbox.setEnabled(true);
                        InjectionNotIPCheckbox.setEnabled(true);
                    }else {
                        removeFromCmd(M_Injection);
                        removeFromCmd(M_Injection_Preserve_Cache);
                        removeFromCmd(M_Injection_Per_Domain);
                        removeFromCmd(M_Injection_JSURL + JSURLtext);
                        removeFromCmd(M_Injection_HTMLURL + HTMLURLtext);
                        removeFromCmd(M_Injection_HTMLPAY + HTMLPAYtext);
                        removeFromCmd(M_Injection_Match + MATCHtext);
                        removeFromCmd(M_Injection_Rate_Limit + RATEtext);
                        removeFromCmd(M_Injection_Number + NUMtext);
                        removeFromCmd(M_Injection_Only_IP + ONLYIPtext);
                        removeFromCmd(M_Injection_Not_IP + NOTIPtext);

                        /* Uncheck Checkboxes */
                        InjectionPreserveCacheCheckbox.setChecked(false);
                        InjectionPerDomainCheckbox.setChecked(false);
                        InjectionJSURLCheckbox.setChecked(false);
                        InjectionHTMLURLCheckbox.setChecked(false);
                        InjectionHTMLPAYCheckbox.setChecked(false);
                        InjectionMatchCheckbox.setChecked(false);
                        InjectionRateLimitCheckbox.setChecked(false);
                        InjectionNumberCheckbox.setChecked(false);
                        InjectionOnlyIPCheckbox.setChecked(false);
                        InjectionNotIPCheckbox.setChecked(false);

                        /* Don't allow checkboxes to be enabled if Injection checkbox not activated */
                        InjectionPreserveCacheCheckbox.setEnabled(false);
                        InjectionPerDomainCheckbox.setEnabled(false);
                        InjectionJSURLCheckbox.setEnabled(false);
                        InjectionHTMLURLCheckbox.setEnabled(false);
                        InjectionHTMLPAYCheckbox.setEnabled(false);
                        InjectionMatchCheckbox.setEnabled(false);
                        InjectionRateLimitCheckbox.setEnabled(false);
                        InjectionNumberCheckbox.setEnabled(false);
                        InjectionOnlyIPCheckbox.setEnabled(false);
                        InjectionNotIPCheckbox.setEnabled(false);
                    }
                }
            };
            InjectionCheckbox.setOnClickListener(checkBoxListener);

            /* Set Checkboxes off by default */
            InjectionPreserveCacheCheckbox.setEnabled(false);
            InjectionPerDomainCheckbox.setEnabled(false);
            InjectionJSURLCheckbox.setEnabled(false);
            InjectionHTMLURLCheckbox.setEnabled(false);
            InjectionHTMLPAYCheckbox.setEnabled(false);
            InjectionMatchCheckbox.setEnabled(false);
            InjectionRateLimitCheckbox.setEnabled(false);
            InjectionNumberCheckbox.setEnabled(false);
            InjectionOnlyIPCheckbox.setEnabled(false);
            InjectionNotIPCheckbox.setEnabled(false);

            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }


    public static class MITMfSpoof extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_spoof, container, false);


            // Redirecct Spinner
            final Spinner redirectSpinner = (Spinner) rootView.findViewById(R.id.mitmf_spoof_redirectspin);
            ArrayAdapter<CharSequence> redirectAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.mitmf_spoof_type, android.R.layout.simple_spinner_item);
            redirectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            redirectSpinner.setAdapter(redirectAdapter);
            redirectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String selectedItemText = parent.getItemAtPosition(pos).toString();
                    Log.d("Slected: ", selectedItemText);
                    switch (pos) {
                        case 0:
                            removeFromCmd(M_Spoofer_Redirect);
                            break;
                        case 1:
                            // ARP
                            removeFromCmd(M_Spoofer_Redirect);
                            M_Spoofer_Redirect = " --arp";
                            addToCmd(M_Spoofer_Redirect);
                            break;
                        case 2:
                            // ICMP
                            removeFromCmd(M_Spoofer_Redirect);
                            M_Spoofer_Redirect = " --icmp";
                            addToCmd(M_Spoofer_Redirect);
                            break;
                        case 3:
                            // DHCP
                            removeFromCmd(M_Spoofer_Redirect);
                            M_Spoofer_Redirect = " --dhcp";
                            addToCmd(M_Spoofer_Redirect);
                            break;
                        case 4:
                            // DNS
                            M_Spoofer_Redirect = " --dns";
                            addToCmd(M_Spoofer_Redirect);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //Another interface callback
                }
            });

            // ARP Mode Spinner
            final Spinner ARPSpinner = (Spinner) rootView.findViewById(R.id.mitmf_spoof_arpmodespin);
            ArrayAdapter<CharSequence> arpAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.mitmf_spoof_arpmode, android.R.layout.simple_spinner_item);
            arpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ARPSpinner.setAdapter(arpAdapter);
            ARPSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String selectedItemText = parent.getItemAtPosition(pos).toString();
                    Log.d("Slected: ", selectedItemText);
                    switch (pos) {
                        case 0:
                            // Nothing
                            removeFromCmd(M_Spoofer_ARP_Mode);
                            break;
                        case 1:
                            // ARP Request REQ
                            removeFromCmd(M_Spoofer_ARP_Mode);
                            M_Spoofer_ARP_Mode = " --arpmode req";
                            addToCmd(M_Spoofer_ARP_Mode);
                            break;
                        case 2:
                            // ARP Reply REP
                            removeFromCmd(M_Spoofer_ARP_Mode);
                            M_Spoofer_ARP_Mode = " --arpmode rep";
                            addToCmd(M_Spoofer_ARP_Mode);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //Another interface callback
                }
            });

            // Textfield for Spoof Gateway
            M_Spoofer_Gateway_Text = (EditText) rootView.findViewById(R.id.mitmf_spoof_gateway_text);
            M_Spoofer_Gateway_Text.setEnabled(false);
            // Detect changes to TextField
            M_Spoofer_Gateway_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Spoofer_Gateway + GATEtext);
                    M_Spoofer_Gateway = "--gateway ";
                    GATEtext = M_Spoofer_Gateway_Text.getText().toString();
                    addToCmd(M_Spoofer_Gateway + GATEtext);
                }
            });

            // Checkbox for Spoof Gateway
            final CheckBox SpoofGatewayIPCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_spoof_gateway);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SpoofGatewayIPCheckbox.isChecked()) {
                        M_Spoofer_Gateway = "--gateway ";
                        M_Spoofer_Gateway_Text.setFocusable(true);
                        M_Spoofer_Gateway_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Spoofer_Gateway + GATEtext);
                        M_Spoofer_Gateway_Text.setFocusable(false);
                        M_Spoofer_Gateway_Text.setEnabled(false);

                    }
                }
            };
            SpoofGatewayIPCheckbox.setOnClickListener(checkBoxListener);

            // Textfield for Spoof Targets
            M_Spoofer_Targets_Text = (EditText) rootView.findViewById(R.id.mitmf_spoof_targets_text);
            M_Spoofer_Targets_Text.setEnabled(false);
            // Detect changes to TextField
            M_Spoofer_Targets_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Spoofer_Targets + TARGETtext);
                    M_Spoofer_Targets = " --targets ";
                    TARGETtext = M_Spoofer_Targets_Text.getText().toString();
                    addToCmd(M_Spoofer_Targets + TARGETtext);
                }
            });

            final CheckBox SpoofSpecifyTargetCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_spoof_targets);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SpoofSpecifyTargetCheckbox.isChecked()) {
                        M_Spoofer_Targets = " --targets ";
                        M_Spoofer_Targets_Text.setFocusable(true);
                        M_Spoofer_Targets_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Spoofer_Targets + TARGETtext);
                        M_Spoofer_Targets_Text.setFocusable(false);
                        M_Spoofer_Targets_Text.setEnabled(false);

                    }
                }
            };
            SpoofSpecifyTargetCheckbox.setOnClickListener(checkBoxListener);

            // Textfield for Shellshock Command
            M_Spoofer_Shellshock_Text = (EditText) rootView.findViewById(R.id.mitmf_spoof_shellshock_text);
            M_Spoofer_Shellshock_Text.setEnabled(false);
            // Detect changes to TextField
            M_Spoofer_Shellshock_Text.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    removeFromCmd(M_Spoofer_Shellshock + SHELLtext);
                    M_Spoofer_Shellshock = " --shellshock ";
                    SHELLtext = M_Spoofer_Shellshock_Text.getText().toString();
                    addToCmd(M_Spoofer_Shellshock + SHELLtext);
                }
            });

            final CheckBox SpoofShellshockCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_spoof_shellshock);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SpoofShellshockCheckbox.isChecked()) {
                        M_Spoofer_Shellshock = " --shellshock ";
                        M_Spoofer_Shellshock_Text.setFocusable(true);
                        M_Spoofer_Shellshock_Text.setEnabled(true);
                    }else{
                        removeFromCmd(M_Spoofer_Shellshock + SHELLtext);
                        M_Spoofer_Shellshock_Text.setFocusable(false);
                        M_Spoofer_Shellshock_Text.setEnabled(false);

                    }
                }
            };
            SpoofShellshockCheckbox.setOnClickListener(checkBoxListener);


            // Checkbox for Spoofer
            final CheckBox SpoofCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_enablespoof);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(SpoofCheckbox.isChecked()) {
                        M_Spoofer = " --spoof";
                        addToCmd(M_Spoofer);
                        /* Allow checkboxes to be enabled if Spoof checkbox activated */
                        redirectSpinner.setEnabled(true);
                        ARPSpinner.setEnabled(true);
                        SpoofGatewayIPCheckbox.setChecked(true);
                        SpoofGatewayIPCheckbox.setEnabled(true);
                        SpoofSpecifyTargetCheckbox.setEnabled(true);
                        SpoofShellshockCheckbox.setEnabled(true);
                    }else{
                        removeFromCmd(M_Spoofer);
                        removeFromCmd(M_Spoofer_Redirect);
                        removeFromCmd(M_Spoofer_ARP_Mode);
                        removeFromCmd(M_Spoofer_Gateway + GATEtext);
                        removeFromCmd(M_Spoofer_Targets + TARGETtext);
                        removeFromCmd(M_Spoofer_Shellshock + SHELLtext);

                        /* Uncheck Checkboxes */
                        SpoofGatewayIPCheckbox.setChecked(false);
                        SpoofSpecifyTargetCheckbox.setChecked(false);
                        SpoofShellshockCheckbox.setChecked(false);

                        /* Don't allow checkboxes to be enabled if Spoof checkbox not activated */
                        redirectSpinner.setEnabled(false);
                        ARPSpinner.setEnabled(false);
                        SpoofGatewayIPCheckbox.setEnabled(false);
                        SpoofSpecifyTargetCheckbox.setEnabled(false);
                        SpoofShellshockCheckbox.setEnabled(false);
                    }
                }
            };
            SpoofCheckbox.setOnClickListener(checkBoxListener);

            /* Enable by default until spoof button is clicked */
            redirectSpinner.setEnabled(false);
            ARPSpinner.setEnabled(false);
            SpoofGatewayIPCheckbox.setEnabled(false);
            SpoofSpecifyTargetCheckbox.setEnabled(false);
            SpoofShellshockCheckbox.setEnabled(false);

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

            // Checkbox for Responder Analyze
            final CheckBox ResponderAnalyzeCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_analyze);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderAnalyzeCheckbox.isChecked()) {
                        M_Responder_Analyze = " --analyze";
                        addToCmd(M_Responder_Analyze);
                    }else{
                        removeFromCmd(M_Responder_Analyze);
                    }
                }
            };
            ResponderAnalyzeCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for Responder Fingerprint
            final CheckBox ResponderFingerprintCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_fingerprint);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderFingerprintCheckbox.isChecked()) {
                        M_Responder_Fingerprint = " --fingerprint";
                        addToCmd(M_Responder_Fingerprint);
                    }else{
                        removeFromCmd(M_Responder_Fingerprint);
                    }
                }
            };
            ResponderFingerprintCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for Responder Downgrade
            final CheckBox ResponderDowngradeCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_LM);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderDowngradeCheckbox.isChecked()) {
                        M_Responder_Downgrade = " --lm";
                        addToCmd(M_Responder_Downgrade);
                    }else{
                        removeFromCmd(M_Responder_Downgrade);
                    }
                }
            };
            ResponderDowngradeCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for NBTNS
            final CheckBox ResponderNBTNSCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_NBTNS);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderNBTNSCheckbox.isChecked()) {
                        M_Responder_NBTNS = " --nbtns";
                        addToCmd(M_Responder_NBTNS);
                    }else{
                        removeFromCmd(M_Responder_NBTNS);
                    }
                }
            };
            ResponderNBTNSCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for WAPD
            final CheckBox ResponderWPADCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_WPAD);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderWPADCheckbox.isChecked()) {
                        M_Responder_WPAD = " --wpad";
                        addToCmd(M_Responder_WPAD);
                    }else{
                        removeFromCmd(M_Responder_WPAD);
                    }
                }
            };
            ResponderWPADCheckbox.setOnClickListener(checkBoxListener);

            // Checkbox for Responder WREDIR
            final CheckBox ResponderWRedirCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder_WREDIR);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderWRedirCheckbox.isChecked()) {
                        M_Responder_WRedir = " --wredir";
                        addToCmd(M_Responder_WRedir);
                    }else{
                        removeFromCmd(M_Responder_WRedir);
                    }
                }
            };
            ResponderWRedirCheckbox.setOnClickListener(checkBoxListener);


            // Checkbox for Responder
            final CheckBox ResponderCheckbox = (CheckBox) rootView.findViewById(R.id.mitmf_responder);
            checkBoxListener =new View.OnClickListener() {
                public void onClick(View v) {
                    if(ResponderCheckbox.isChecked()) {
                        M_Responder = " --responder";
                        addToCmd(M_Responder);
                        /* Allow checkboxes to be enabled if Responder not activated */
                        ResponderAnalyzeCheckbox.setEnabled(true);
                        ResponderFingerprintCheckbox.setEnabled(true);
                        ResponderDowngradeCheckbox.setEnabled(true);
                        ResponderNBTNSCheckbox.setEnabled(true);
                        ResponderWPADCheckbox.setEnabled(true);
                        ResponderWRedirCheckbox.setEnabled(true);
                    }else{
                        removeFromCmd(M_Responder);
                        removeFromCmd(M_Responder_Analyze);
                        removeFromCmd(M_Responder_Fingerprint);
                        removeFromCmd(M_Responder_Downgrade);
                        removeFromCmd(M_Responder_NBTNS);
                        removeFromCmd(M_Responder_WPAD);
                        removeFromCmd(M_Responder_WRedir);

                        /* Uncheck all responder checkboxes */
                        ResponderAnalyzeCheckbox.setChecked(false);
                        ResponderFingerprintCheckbox.setChecked(false);
                        ResponderDowngradeCheckbox.setChecked(false);
                        ResponderNBTNSCheckbox.setChecked(false);
                        ResponderWPADCheckbox.setChecked(false);
                        ResponderWRedirCheckbox.setChecked(false);

                        /* Don't allow checkboxes to be enabled if Responder not activated */
                        ResponderAnalyzeCheckbox.setEnabled(false);
                        ResponderFingerprintCheckbox.setEnabled(false);
                        ResponderDowngradeCheckbox.setEnabled(false);
                        ResponderNBTNSCheckbox.setEnabled(false);
                        ResponderWPADCheckbox.setEnabled(false);
                        ResponderWRedirCheckbox.setEnabled(false);                    }
                }
            };
            ResponderCheckbox.setOnClickListener(checkBoxListener);

            /* Set default responder options to not activated */
            ResponderAnalyzeCheckbox.setEnabled(false);
            ResponderFingerprintCheckbox.setEnabled(false);
            ResponderDowngradeCheckbox.setEnabled(false);
            ResponderNBTNSCheckbox.setEnabled(false);
            ResponderWPADCheckbox.setEnabled(false);
            ResponderWRedirCheckbox.setEnabled(false);


            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class MITMfConfigFragment extends Fragment {

        private String configFilePath = nh.CHROOT_PATH +"/etc/mitmf/mitmf.conf";
        ShellExecuter exe = new ShellExecuter();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mitmf_config);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);


            EditText source = (EditText) rootView.findViewById(R.id.source);
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) rootView.findViewById(R.id.source);
                    Boolean isSaved = exe.SaveFileContents(source.getText().toString(), configFilePath);
                    if(isSaved){
                        nh.showMessage("Source updated");
                    } else {
                        nh.showMessage("Source not updated");
                    }
                }
            });
            return rootView;
        }
    }

    private String getCmd(){
        String genCmd = "";
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
            genCmd = genCmd + CommandComposed.get(j);
        }
        Log.d("MITMF CMD OUTPUT: ", "mitmf " + genCmd);

        return genCmd;
    }
    private static void cleanCmd() {
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
                CommandComposed.remove(j);
        }
    }
    private static void addToCmd(String opt) {
        CommandComposed.add(opt);
    }
    private static void removeFromCmd(String opt) {
        for (int j = CommandComposed.size()-1; j >= 0; j--) {
            if(CommandComposed.get(j).equals(opt))
                CommandComposed.remove(j);
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
