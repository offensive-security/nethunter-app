package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

public class DuckHunterFragment extends Fragment implements ActionBar.TabListener {

    private ViewPager mViewPager;
    private static SharedPreferences sharedpreferences;

    // Language vars
    private final static CharSequence[] languages = {"American English", "French", "German", "Spanish", "Swedish", "Italian", "British English", "Russian", "Danish", "Norwegian", "Portugese", "Belgian", "Canadian Multilingual", "Canadian"};
    private static String lang = "us"; // Set US as default language
    private static Boolean shouldConvert = true;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "DuckHunterFragment";
    private static NhPaths nh;
    private static String prwText = "";
    private boolean isHIDenable = false;

    public DuckHunterFragment() {
    }

    public static DuckHunterFragment newInstance(int sectionNumber) {
        DuckHunterFragment fragment = new DuckHunterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        nh = new NhPaths();

        View rootView = inflater.inflate(R.layout.duck_hunter, container, false);
        DuckHunterFragment.TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = rootView.findViewById(R.id.pagerDuckHunter);
        mViewPager.setAdapter(tabsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
            }
        });
        setHasOptionsMenu(true);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
	check_HID_enable();
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.duck_hunter, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            menu.findItem(R.id.duckConvertAttack).setVisible(true);
        } else {
            menu.findItem(R.id.duckConvertAttack).setVisible(false);
        }
        getActivity().invalidateOptionsMenu();
    }

    private void setLang() {
        int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);
        switch (keyboardLayoutIndex) {
            case 1:
                lang = "fr";
                break;
            case 2:
                lang = "de";
                break;
            case 3:
                lang = "es";
                break;
            case 4:
                lang = "sv";
                break;
            case 5:
                lang = "it";
                break;
            case 6:
                lang = "uk";
                break;
            case 7:
                lang = "ru";
                break;
            case 8:
                lang = "dk";
                break;
            case 9:
                lang = "no";
                break;
            case 10:
                lang = "pt";
                break;
            case 11:
                lang = "be";
                break;
            case 12:
                lang = "cm";
                break;
            case 13:
                lang = "ca";
                break;
            default:
                lang = "us";
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.duckConvertAttack:
                if (isHIDenable) {
                    setLang();
                    nh.showMessage("Launching Attack");
                    if (getView() == null) {
                        return true;
                    }
                    final View v = getView();
                    new Thread(new Runnable() {
                        public void run() {
                            if (shouldConvert) {
                                convert();
                                try {
                                    Thread.sleep(2000);  // Slow down
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            start();
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    nh.showMessage("Attack launched!");
                                }
                            });
                        }
                    }
                    }).start();
                } else {
                    nh.showMessage_long("HID interfaces are not enabled or something wrong with the permission of /dev/hidg*, make sure they are enabled and permissions are granted as 666");
                }

                return true;
            case R.id.chooseLanguage:
                openLanguageDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static Boolean updatefile() {
        try {
            File myFile = new File(nh.APP_SD_FILES_PATH, DuckHunterConvertFragment.configFilePath);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(prwText);
            myOutWriter.close();
            fOut.close();
            return true;
        } catch (Exception e) {
            nh.showMessage(e.getMessage());
            return false;
        }

    }

    private static void convert() {
        ShellExecuter exe = new ShellExecuter();
        if (updatefile()) {
            String[] command = new String[1];
            Log.d(TAG, lang);
            command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali duck-hunt-convert " + lang +
                    " /sdcard/nh_files/modules/duckconvert.txt " + "/opt/" +
                    DuckHunterPreviewFragment.configFileFilename + "'";
            exe.RunAsRoot(command);
        }
    }

    private void start() {
        String[] command = new String[1];
        command[0] = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali duck-hunt-run /opt/duckout.sh'";
        String command_string = "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali duck-hunt-run /opt/duckout.sh'";
        Log.d(TAG, command_string);
        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRoot(command);
    }

    private void openLanguageDialog() {

        int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Language:");
        builder.setPositiveButton("OK", (dialog, which) -> {
            setLang();
            if (mViewPager.getCurrentItem() == 1) {
                if (getView() == null) {
                    return;
                }

                final TextView source = getView().findViewById(R.id.source);
                source.setText("Loading wait...");
                new Thread(() -> {
                    convert();
                    String output = "";
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Process p = Runtime.getRuntime().exec("su -c cat " + DuckHunterPreviewFragment.configFilePath + DuckHunterPreviewFragment.configFileFilename);
                        p.waitFor();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output = output + line + "\n";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final String finalOutput = output;
                    source.post(() -> source.setText(finalOutput));
                }).start();
            } else {
                new Thread(() -> convert()).start();
            }
        });

        builder.setSingleChoiceItems(languages, keyboardLayoutIndex, (dialog, which) -> {
            Editor editor = sharedpreferences.edit();
            editor.putInt("DuckHunterLanguageIndex", which);
            editor.apply();
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

    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {

        TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 1:
                    return new DuckHunterPreviewFragment();
                default:
                    return new DuckHunterConvertFragment();
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
                    return "Preview";
                default:
                    return "Convert";
            }
        }
    }

    public static class DuckHunterConvertFragment extends Fragment implements View.OnClickListener {

        public static final String configFilePath = "/modules/duckconvert.txt";
        public static final String loadFilePath = "/scripts/ducky/";
        private static final int PICKFILE_RESULT_CODE = 1;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.duck_hunter_convert, container, false);

            TextView t2 = rootView.findViewById(R.id.reference_text);
            t2.setMovementMethod(LinkMovementMethod.getInstance());

            EditText source = rootView.findViewById(R.id.editSource);

            String duckyscript_file[] = getDuckyScriptFiles();
            source.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    prwText = s.toString();
                    shouldConvert = true;
                }
            });
            //File appFolder = getActivity().getFilesDir();
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

            Button b = rootView.findViewById(R.id.duckyLoad);
            Button b1 = rootView.findViewById(R.id.duckySave);
            b.setOnClickListener(this);
            b1.setOnClickListener(this);


            // Duckhunter preset spinner templates
            Spinner duckyscriptSpinner = (Spinner) rootView.findViewById(R.id.duckhunter_preset_spinner);
            ArrayAdapter<String> duckyscriptAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, duckyscript_file);
            duckyscriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            duckyscriptSpinner.setAdapter(duckyscriptAdapter);
            duckyscriptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    //String selectedItemText = parent.getSelectedItem().toString();
                    getPreset(duckyscriptSpinner.getSelectedItem().toString());
                    //switch (pos) {
                    //   case 0:
                    //        break;
                    //    case 1:
                    //        getPreset("helloworld"); // Hello World!
                    //        break;
                    //    case 2:
                    //        getPreset("osx_perl_reverse_shell"); // OSX Perl: Reverse Shell
                    //        break;
                    //    case 3:
                    //        getPreset("osx_ruby_reverse_shell"); // OSX Ruby: Reverse Shell
                    //        break;
                    //    case 4:
                    //        getPreset("windows_rdp"); // Enable RDP in Windows
                    //        break;
		    //	  case 5:
		    //	      getPreset("fake_win10_update");
                    //}
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //Another interface callback
                }
            });

            return rootView;
        }

        private void getPreset(String filename) {
            if (getView() == null) {
                return;
            }
            String filename_path = "/duckyscripts/";
            filename = filename_path + filename;
            EditText source = getView().findViewById(R.id.editSource);
            File file = new File(nh.APP_SD_FILES_PATH, filename);
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
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.duckyLoad:
                    try {

                        File scriptsDir = new File(nh.APP_SD_FILES_PATH, loadFilePath);
                        if (!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                        nh.showMessage(e.getMessage());
                    }
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri selectedUri = Uri.parse(nh.APP_SD_FILES_PATH + loadFilePath);
                    intent.setDataAndType(selectedUri, "file/*");
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                    break;
                case R.id.duckySave:
                    try {

                        File scriptsDir = new File(nh.APP_SD_FILES_PATH, loadFilePath);
                        if (!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                        nh.showMessage(e.getMessage());
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    alert.setTitle("Name");
                    alert.setMessage("Please enter a name for your script.");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(getActivity());
                    alert.setView(input);

                    alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                        String value = input.getText().toString();
                        if (value.length() > 0) {
                            //Save file (ask name)
                            File scriptFile = new File(nh.APP_SD_FILES_PATH + loadFilePath + File.separator + value + ".conf");
                            System.out.println(scriptFile.getAbsolutePath());
                            if (!scriptFile.exists()) {
                                try {
                                    if (getView() != null) {
                                        EditText source = getView().findViewById(R.id.editSource);
                                        String text = source.getText().toString();
                                        scriptFile.createNewFile();
                                        FileOutputStream fOut = new FileOutputStream(scriptFile);
                                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                        myOutWriter.append(text);
                                        myOutWriter.close();
                                        fOut.close();
                                        nh.showMessage("Script saved");
                                    }
                                } catch (Exception e) {
                                    nh.showMessage(e.getMessage());
                                }
                            } else {
                                nh.showMessage("File already exists");
                            }
                        } else {
                            nh.showMessage("Wrong name provided");
                        }
                    });

                    alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
                        ///Do nothing
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
                        if (getView() == null) {
                            return;
                        }
                        String FilePath = data.getData().getPath();
                        EditText source = getView().findViewById(R.id.editSource);
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

        private String[] getDuckyScriptFiles() {
            ArrayList<String> result = new ArrayList<String>();
            File script_folder = new File("/sdcard/nh_files/duckyscripts");
            File[] filesInFolder = script_folder.listFiles();
            for (File file : filesInFolder) {
                if (!file.isDirectory()) {
                    result.add(file.getName());
                }
            }
            return result.toArray(new String[0]);
        }
    } //end of class


    public static class DuckHunterPreviewFragment extends Fragment {

        // Error reading chroot_path
        //public static final String configFilePath = nh.CHROOT_PATH + "/opt/";
        public static final String configFilePath = "/data/local/nhsystem/kali-armhf/opt/";
        public static final String configFileFilename = "duckout.sh";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.duck_hunter_preview, container, false);
            readFileForPreview();

            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            if (isVisibleToUser) {
                //Log.d("ISORNOT", isVisibleToUser + prwText);
                readFileForPreview();
            }
            super.setUserVisibleHint(isVisibleToUser);
        }

        public void readFileForPreview() {
            if (getView() == null) {
                return;
            }

            final TextView source = getView().findViewById(R.id.source);
            source.setText(R.string.loading_wait);
            new Thread(() -> {
                String output = "";
                if (shouldConvert) {
                    convert();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Process p = Runtime.getRuntime().exec("su -c cat " + configFilePath + configFileFilename);
                    p.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output = output + line + "\n";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final String finalOutput = output;
                source.post(() -> {
                    source.setText(finalOutput);
                    shouldConvert = false;
                });
            }).start();

        }
    }


    private void check_HID_enable() {
        new Thread(new Runnable() {
             public void run() {
                ShellExecuter exe_check = new ShellExecuter();
                String hidgs[] = {"/dev/hidg0", "/dev/hidg1"};
                for (String hidg : hidgs) {
                    if (!exe_check.RunAsRootOutput("su -c \"stat -c '%a' " + hidg + "\"").equals("666")) {
                        isHIDenable = false;
                        break;
                    }
                    isHIDenable = true;
                }
            }
        }).start();
    }
}
