package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class ManaFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter tabsPagerAdapter;
    ViewPager mViewPager;

    private Integer selectedScriptIndex = 0;
    final CharSequence[] scripts = {"mana-nat-full", "mana-nat-simple", "mana-nat-simple-bdf"};

    String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/etc/mana-toolkit/hostapd-karma.conf";

    private static final String ARG_SECTION_NUMBER = "section_number";
    private String fileDir;


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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        if (isAdded()) {
            fileDir = getActivity().getFilesDir().toString() + "/scripts";
        }
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
                i.putExtra("shell", true);
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
                String[] command = new String[1];
                switch (selectedScriptIndex) {
                    case 0:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-full-lollipop start'";
                        } else {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-full-kitkat start'";
                        }
                        break;
                    case 1:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-simple-lollipop start'";
                        } else {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-simple-kitkat start'";
                        }
                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-bdf-lollipop start'";
                        } else {
                            command[0] = "su -c '" + fileDir + "/bootkali mana-bdf-kitkat start'";
                        }
                        break;
                    default:
                        ((AppNavHomeActivity) getActivity()).showMessage("Invalid script!");
                        return;
                }
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(command);
                ((AppNavHomeActivity) getActivity()).showMessage("Attack executed!");
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
            command[0] = "su -c '" + fileDir + "/bootkali mana-lollipop stop'";
        } else {
            command[0] = "su -c '" + fileDir + "/bootkali mana-kitkat stop'";
        }
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Mana Stopped");
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
        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/etc/mana-toolkit/hostapd-karma.conf";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mana_hostapd, container, false);
            loadOptions(rootView);

            //Update button
            Button button = (Button) rootView.findViewById(R.id.updateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShellExecuter exe = new ShellExecuter();
                    String source = exe.Executer("cat " + configFilePath);

                    EditText ifc = (EditText) getView().findViewById(R.id.ifc);
                    EditText bssid = (EditText) getView().findViewById(R.id.bssid);
                    EditText ssid = (EditText) getView().findViewById(R.id.ssid);
                    EditText channel = (EditText) getView().findViewById(R.id.channel);
                    EditText enableKarma = (EditText) getView().findViewById(R.id.enable_karma);
                    EditText karmaLoud = (EditText) getView().findViewById(R.id.karma_loud);

                    source = source.replaceAll("(?m)^interface=(.*)$", "interface=" + ifc.getText().toString());
                    source = source.replaceAll("(?m)^bssid=(.*)$", "bssid=" + bssid.getText().toString());
                    source = source.replaceAll("(?m)^ssid=(.*)$", "ssid=" + ssid.getText().toString());
                    source = source.replaceAll("(?m)^channel=(.*)$", "channel=" + channel.getText().toString());
                    source = source.replaceAll("(?m)^enable_karma=(.*)$", "enable_karma=" + enableKarma.getText().toString());
                    source = source.replaceAll("(?m)^karma_loud=(.*)$", "karma_loud=" + karmaLoud.getText().toString());

                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + source + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Options updated!");
                }
            });
            return rootView;
        }


        public void loadOptions(View rootView) {
            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            /*
             * Interface
             */
            EditText ifc = (EditText) rootView.findViewById(R.id.ifc);
            String regExpatInterface = "^interface=(.*)$";
            Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
            Matcher matcherIfc = patternIfc.matcher(text);
            if (matcherIfc.find()) {
                String ifcValue = matcherIfc.group(1);
                ifc.setText(ifcValue);
            }

	            /*
                 * bssid
	             */
            EditText bssid = (EditText) rootView.findViewById(R.id.bssid);
            String regExpatbssid = "^bssid=(.*)$";
            Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
            Matcher matcherBssid = patternBssid.matcher(text);
            if (matcherBssid.find()) {
                String bssidVal = matcherBssid.group(1);
                bssid.setText(bssidVal);
            }
	            /*
	             * ssid
	             */
            EditText ssid = (EditText) rootView.findViewById(R.id.ssid);
            String regExpatssid = "^ssid=(.*)$";
            Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
            Matcher matcherSsid = patternSsid.matcher(text);
            if (matcherSsid.find()) {
                String ssidVal = matcherSsid.group(1);
                ssid.setText(ssidVal);
            }
	            /*
	             * channel
	             */
            EditText channel = (EditText) rootView.findViewById(R.id.channel);
            String regExpatChannel = "^channel=(.*)$";
            Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
            Matcher matcherChannel = patternChannel.matcher(text);
            if (matcherChannel.find()) {
                String channelVal = matcherChannel.group(1);
                channel.setText(channelVal);
            }
	            /*
	             * enable_karma
	             */
            EditText enableKarma = (EditText) rootView.findViewById(R.id.enable_karma);
            String regExpatEnableKarma = "^enable_karma=(.*)$";
            Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
            Matcher matcherEnableKarma = patternEnableKarma.matcher(text);
            if (matcherEnableKarma.find()) {
                String enableKarmaVal = matcherEnableKarma.group(1);
                enableKarma.setText(enableKarmaVal);
            }

	            /*
	             * karma_loud
	             */
            EditText karmaLoud = (EditText) rootView.findViewById(R.id.karma_loud);
            String regExpatKarmaLoud = "^karma_loud=(.*)$";
            Pattern patternKarmaLoud = Pattern.compile(regExpatKarmaLoud, Pattern.MULTILINE);
            Matcher matcherKarmaLoud = patternKarmaLoud.matcher(text);
            if (matcherKarmaLoud.find()) {
                String karmaLoudVal = matcherKarmaLoud.group(1);
                karmaLoud.setText(karmaLoudVal);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            loadOptions(getView().getRootView());
        }
    }


    public static class DhcpdFragment extends Fragment {

        private String configFilePath = "files/dhcpd.conf";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_dhcpd);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, configFilePath);
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
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File myFile = new File(sdcard, configFilePath);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        EditText source = (EditText) rootView.findViewById(R.id.source);
                        myOutWriter.append(source.getText());
                        myOutWriter.close();
                        fOut.close();
                        ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                    } catch (Exception e) {
                        ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                    }
                }
            });
            return rootView;
        }
    }

    public static class DnsspoofFragment extends Fragment {

        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/etc/mana-toolkit/dnsspoof.conf";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            String description = getResources().getString(R.string.mana_dnsspoof);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaNatFullFragment extends Fragment {

        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/usr/share/mana-toolkit/run-mana/start-nat-full-mod.sh";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            String description = getResources().getString(R.string.mana_nat_full);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaNatSimpleFragment extends Fragment {

        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/usr/share/mana-toolkit/run-mana/start-nat-simple.sh";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_nat_simple);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class BdfProxyConfigFragment extends Fragment {

        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/etc/bdfproxy/bdfproxy.cfg";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.bdfproxy_cfg);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

    public static class ManaStartNatSimpleBdfFragment extends Fragment {

        private String configFilePath = getActivity().getFilesDir() + "/chroot/kali-armhf/usr/share/mana-toolkit/run-mana/start-nat-simple-bdf.sh";


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mana_nat_simple_bdf);
            TextView desc = (TextView) rootView.findViewById(R.id.description);
            desc.setText(description);

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);

            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                }
            });
            return rootView;
        }
    }

}