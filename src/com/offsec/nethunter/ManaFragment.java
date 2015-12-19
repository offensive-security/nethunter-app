package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class ManaFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter tabsPagerAdapter;
    ViewPager mViewPager;

    private Integer selectedScriptIndex = 0;
    final CharSequence[] scripts = {"mana-nat-full", "mana-nat-simple", "mana-nat-simple-bdf"};
    private static final String TAG = "ManaFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    static NhPaths nh;
    String configFilePath;

    public ManaFragment() {

    }

    public static ManaFragment newInstance(int sectionNumber) {
        ManaFragment fragment = new ManaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nh = new NhPaths();
        View rootView = inflater.inflate(R.layout.mana, container, false);
        tabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerMana);
        mViewPager.setAdapter(tabsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //actionBar.setSelectedNavigationItem(position);
                getActivity().invalidateOptionsMenu();
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        configFilePath = nh.APP_SD_FILES_PATH + "/configs/hostapd-karma.conf";

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mana, menu);
    }


    public void onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            menu.findItem(R.id.source_button).setVisible(true);
        } else {
            menu.findItem(R.id.source_button).setVisible(false);
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.start_service:
                startMana();
                return true;
            case R.id.stop_service:
                stopMana();
                return true;
            case R.id.source_button:
                Intent i = new Intent(getActivity(), EditSourceActivity.class);
                i.putExtra("path", configFilePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startMana() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Script to execute:");
        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (selectedScriptIndex) {
                    // launching mana on the terminal so it doesnt die suddenly
                    case 0:
                        nh.showMessage("Starting MANA NAT FULL");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intentClickListener_NH(nh.makeTermTitle("MANA-FULL") + "/usr/share/mana-toolkit/run-mana/start-nat-full-lollipop.sh");
                        } else {
                            intentClickListener_NH(nh.makeTermTitle("MANA-FULL") + "/usr/share/mana-toolkit/run-mana/start-nat-full-kitkat.sh");
                        }
                        break;
                    case 1:
                        nh.showMessage("Starting MANA NAT SIMPLE");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intentClickListener_NH(nh.makeTermTitle("MANA-SIMPLE") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-lollipop.sh");
                        } else {
                            intentClickListener_NH(nh.makeTermTitle("MANA-SIMPLE") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-kitkat.sh");
                        }
                        break;
                    case 2:
                        nh.showMessage("Starting MANA NAT SIMPLE && BDF");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intentClickListener_NH(nh.makeTermTitle("MANA-BDF") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-lollipop.sh");
                        } else {
                            intentClickListener_NH(nh.makeTermTitle("MANA-BDF") + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-kitkat.sh");
                        }
                        // we wait ~10 secs before launching msf
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        nh.showMessage("Starting MSF with BDF resource.rc");
                                        intentClickListener_NH(nh.makeTermTitle("MSF") + "msfconsole -q -r /usr/share/bdfproxy/bdfproxy_msf_resource.rc");
                                    }
                                }, 10000);
                        break;
                    default:
                        nh.showMessage("Invalid script!");
                        return;
                }
                nh.showMessage("Attack Launched!");
            }
        });
        builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setSingleChoiceItems(scripts, selectedScriptIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedScriptIndex = which;
            }
        });
        builder.show();

    }

    private void stopMana() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali mana-lollipop stop'";
        } else {
            command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali mana-kitkat stop'";
        }
        exe.RunAsRoot(command);
        nh.showMessage("Mana Stopped");
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    //	 public static class TabsPagerAdapter extends FragmentPagerAdapter {
    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {


        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new HostapdFragment();
                case 1:
                    return new DhcpdFragment();
                case 2:
                    return new DnsspoofFragment();
                case 3:
                    return new ManaNatFullFragment();
                case 4:
                    return new ManaNatSimpleFragment();
                case 5:
                    return new BdfProxyConfigFragment();
                default:
                    return new ManaStartNatSimpleBdfFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "hostapd-karma.conf";
                case 1:
                    return "dhcpd.conf";
                case 2:
                    return "dnsspoof.conf";
                case 3:
                    return "nat-mana-full";
                case 4:
                    return "nat-mana-simple";
                case 5:
                    return "bdfproxy.cfg";
                default:
                    return "mana-nat-simple-bdf";
            }
        }
    } //end class


    public static class HostapdFragment extends Fragment {
        // private String configFilePath = nh.CHROOT_PATH + "/etc/hostapd.conf";
        private String configFilePath = nh.APP_SD_FILES_PATH + "/configs/hostapd-karma.conf";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mana_hostapd, container, false);
            Button button = (Button) rootView.findViewById(R.id.updateButton);
            loadOptions(rootView);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShellExecuter exe = new ShellExecuter();
                    File file = new File(configFilePath);
                    String source = null;
                    try {
                        source = Files.toString(file, Charsets.UTF_8);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(getView() == null){
                        return;
                    }
                    EditText ifc = (EditText) getView().findViewById(R.id.ifc);
                    EditText bssid = (EditText) getView().findViewById(R.id.bssid);
                    EditText ssid = (EditText) getView().findViewById(R.id.ssid);
                    EditText channel = (EditText) getView().findViewById(R.id.channel);
                    EditText enableKarma = (EditText) getView().findViewById(R.id.enable_karma);
                    EditText karmaLoud = (EditText) getView().findViewById(R.id.karma_loud);
                    // FIXED BY BINKYBEAR <3
                    if(source != null){
                        source = source.replaceAll("(?m)^interface=(.*)$", "interface=" + ifc.getText().toString());
                        source = source.replaceAll("(?m)^bssid=(.*)$", "bssid=" + bssid.getText().toString());
                        source = source.replaceAll("(?m)^ssid=(.*)$", "ssid=" + ssid.getText().toString());
                        source = source.replaceAll("(?m)^channel=(.*)$", "channel=" + channel.getText().toString());
                        source = source.replaceAll("(?m)^enable_karma=(.*)$", "enable_karma=" + enableKarma.getText().toString());
                        source = source.replaceAll("(?m)^karma_loud=(.*)$", "karma_loud=" + karmaLoud.getText().toString());

                        exe.SaveFileContents(source, configFilePath);
                        nh.showMessage("Source updated");
                    }

                }
            });
            return rootView;
        }


        public void loadOptions(View rootView) {



            final EditText ifc = (EditText) rootView.findViewById(R.id.ifc);
            final EditText bssid = (EditText) rootView.findViewById(R.id.bssid);
            final EditText ssid = (EditText) rootView.findViewById(R.id.ssid);
            final EditText channel = (EditText) rootView.findViewById(R.id.channel);
            final EditText enableKarma = (EditText) rootView.findViewById(R.id.enable_karma);
            final EditText karmaLoud = (EditText) rootView.findViewById(R.id.karma_loud);

            new Thread(new Runnable() {
                public void run() {
                    ShellExecuter exe = new ShellExecuter();
                    String text = exe.ReadFile_SYNC(configFilePath);

                    String regExpatInterface = "^interface=(.*)$";
                    Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
                    final Matcher matcherIfc = patternIfc.matcher(text);

                    String regExpatbssid = "^bssid=(.*)$";
                    Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
                    final Matcher matcherBssid = patternBssid.matcher(text);

                    String regExpatssid = "^ssid=(.*)$";
                    Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
                    final Matcher matcherSsid = patternSsid.matcher(text);

                    String regExpatChannel = "^channel=(.*)$";
                    Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
                    final Matcher matcherChannel = patternChannel.matcher(text);

                    String regExpatEnableKarma = "^enable_karma=(.*)$";
                    Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
                    final Matcher matcherEnableKarma = patternEnableKarma.matcher(text);

                    String regExpatKarmaLoud = "^karma_loud=(.*)$";
                    Pattern patternKarmaLoud = Pattern.compile(regExpatKarmaLoud, Pattern.MULTILINE);
                    final Matcher matcherKarmaLoud = patternKarmaLoud.matcher(text);

                    ifc.post(new Runnable() {
                        @Override
                        public void run() {
                        /*
                         * Interface
                         */
                        if (matcherIfc.find()) {
                            String ifcValue = matcherIfc.group(1);
                            ifc.setText(ifcValue);
                        }
                        /*
                         * bssid
                         */
                        if (matcherBssid.find()) {
                            String bssidVal = matcherBssid.group(1);
                            bssid.setText(bssidVal);
                        }
                        /*
                         * ssid
                         */
                        if (matcherSsid.find()) {
                            String ssidVal = matcherSsid.group(1);
                            ssid.setText(ssidVal);
                        }
                        /*
                         * channel
                         */
                        if (matcherChannel.find()) {
                            String channelVal = matcherChannel.group(1);
                            channel.setText(channelVal);
                        }
                        /*
                         * enable_karma
                         */
                        if (matcherEnableKarma.find()) {
                            String enableKarmaVal = matcherEnableKarma.group(1);
                            enableKarma.setText(enableKarmaVal);
                        }
                       /*
                       * karma_loud
                       */
                        if (matcherKarmaLoud.find()) {
                            String karmaLoudVal = matcherKarmaLoud.group(1);
                            karmaLoud.setText(karmaLoudVal);
                        }
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onResume() {
            super.onResume();

        }
    }

    public static class DhcpdFragment extends Fragment {

        private String configFilePath = nh.CHROOT_PATH +"/etc/dhcp/dhcpd.conf";
        ShellExecuter exe = new ShellExecuter();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_dhcpd);
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

    public static class DnsspoofFragment extends Fragment {

        private String configFilePath;
        ShellExecuter exe = new ShellExecuter();
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.source_short, container, false);
            String description = getResources().getString(R.string.mana_dnsspoof);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            configFilePath = nh.CHROOT_PATH + "/etc/mana-toolkit/dnsspoof.conf";

            EditText source = (EditText) rootView.findViewById(R.id.source);
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();
                    exe.SaveFileContents(newSource, configFilePath);
                    nh.showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaNatFullFragment extends Fragment {

        private String configFilePath;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            TextView desc = (TextView) rootView.findViewById(R.id.description);

            desc.setText(getResources().getString(R.string.mana_nat_full));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = nh.CHROOT_PATH +"/usr/share/mana-toolkit/run-mana/start-nat-full-lollipop.sh";
            } else {
                configFilePath = nh.CHROOT_PATH +"/usr/share/mana-toolkit/run-mana/start-nat-full-kitkat.sh";
            }


            EditText source = (EditText) rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();
                    ShellExecuter exe = new ShellExecuter();
                    exe.SaveFileContents(newSource, configFilePath);
                    nh.showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaNatSimpleFragment extends Fragment {

        private String configFilePath;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = nh.CHROOT_PATH +"/usr/share/mana-toolkit/run-mana/start-nat-simple-lollipop.sh";
            } else {
                configFilePath = nh.CHROOT_PATH +"/usr/share/mana-toolkit/run-mana/start-nat-simple-kitkat.sh";
            }

            String description = getResources().getString(R.string.mana_nat_simple);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);


            EditText source = (EditText) rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();
                    ShellExecuter exe = new ShellExecuter();
                    exe.SaveFileContents(newSource, configFilePath);
                    nh.showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class BdfProxyConfigFragment extends Fragment {

        private String configFilePath;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.bdfproxy_cfg);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);
            // use the good one?
            configFilePath = nh.APP_SD_FILES_PATH + "/configs/bdfproxy.cfg";
            Log.d("BDFPATH", configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();
                    ShellExecuter exe = new ShellExecuter();
                    exe.SaveFileContents(newSource, configFilePath);
                    nh.showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaStartNatSimpleBdfFragment extends Fragment {

        private String configFilePath;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                configFilePath = nh.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-lollipop.sh";
            } else {
                configFilePath = nh.CHROOT_PATH + "/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf-kitkat.sh";
            }
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_nat_simple_bdf);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            ShellExecuter exe = new ShellExecuter();
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();
                    ShellExecuter exe = new ShellExecuter();
                    exe.SaveFileContents(newSource, configFilePath);
                    nh.showMessage("Source updated");
                }
            });
            return rootView;
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
