package com.offsec.nethunter;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

//import android.app.Fragment;
//import android.app.FragmentManager;

public class AppNavHomeActivity extends FragmentActivity
        implements SideMenu.NavigationDrawerCallbacks {

    public final static int NETHUNTER_FRAGMENT = 0;
    public final static int KALILAUNCHER_FRAGMENT = 1;
    public final static int KALISERVICES_FRAGMENT = 2;
    public final static int HIDE_FRAGMENT = 3;
    public final static int DUCKHUNTER_FRAGMENT = 4;
    public final static int BADUSB_FRAGMENT = 5;
    public final static int MANA_FRAGMENT = 6;
    public final static int DNSMASQ_FRAGMENT = 7;
    public final static int MACCHANGER_FRAGMENT = 8;
    public final static int IPTABLES_FRAGMENT = 9;

    public final static String TAG = "AppNavHomeActivity";

    // these must be UTF-8 text-based scripts.  They go in the /assets folder and will be copied
    // to the app's cache area.  Don't make thisstatic or final because it changes below (is reused)
    private String[] SCRIPTS = {"bootkali", "check-kaliapache", "check-kalibeef-xss",
            "check-kalidhcp", "check-kalidnsmq", "check-kalihostapd", "check-kalimetasploit",
            "check-kalissh", "check-kalivnc", "check-kalivpn", "iptables-flush", "killkali",
            "start-apache", "start-badusb-kitkat", "start-badusb-lollipop", "start-beef-xss",
            "start-dhcp", "start-dnsmasq", "start-hid-cmd", "start-hid-cmd-elevated-win7",
            "start-hid-cmd-elevated-win8", "start-hostapd", "start-iptables", "start-msf",
            "start-openvpn", "start-rev-met", "start-rev-met-elevated-win7",
            "start-rev-met-elevated-win8", "start-ssh", "start-update", "start-vpn", "start-web",
            "start-wifite", "stop-apache", "stop-badusb-kitkat", "stop-badusb-lollipop",
            "stop-beef-xss", "stop-dhcp", "stop-dnsmasq", "stop-hostapd", "stop-msf",
            "stop-openvpn", "stop-ssh", "stop-vpn", "stop-web", "etc/init.d/50userinit"};

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private SideMenu mNavigationDrawerFragment;
    private String[] activityNames;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.base_layout);
        //set kali wallpaper as background
        String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/kali-nh/wallpaper/kali-nh-2183x1200.png";
        Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
        ImageView myImageView = (ImageView) findViewById(R.id.bgHome);
        myImageView.setImageBitmap(bitmap);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));
        }

        mNavigationDrawerFragment = (SideMenu)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        String[][] activitiesInfo = mNavigationDrawerFragment.getMenuInfo();
        activityNames = activitiesInfo[0];
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // copy script files, but off the main UI thread
        final Runnable r = new Runnable() {
            public void run() {
                int index = 0;
                for (String script : SCRIPTS) {
                    setupScript(script, "700", index);
                    index++;
                }
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(SCRIPTS);
            }
        };
        Thread t = new Thread(r);
        t.start();

        createDirIfNeeded(Environment.getExternalStorageDirectory() + "/files");
        createDirIfNeeded(getCacheDir() + "/etc/init.d");

    }

    private void createDirIfNeeded(String path) {

        // now make sure the sdcard has the needed /files directory
        File dir = new File(path);
        try {
            if (!dir.exists()) {
                Log.d(TAG, "Couldn't find " + path + " directory.  Making it....");
                if (dir.mkdirs()) {
                    Log.d(TAG, "Made needed directory!");
                } else {
                    Log.d(TAG, "Failed making directory.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR verifying/creating " + path + " directory on sdcard!", e);
        }
    }

    private boolean setupScript(final String scriptName, final String permissions, final int index) {

        // This copies shell script files from /assets/ in the source to the app cache.  It also
        // sets their permission.  It's a boolean in case we need to wait for success/failure.

        // Update these files EVERY restart of app just in case it gets updated by a new apk with
        // changed scripts...

        // thanks to the discussion at:
        // http://stackoverflow.com/questions/8474821/how-to-get-the-android-path-string-to-a-file-on-assets-folder
        // which suggested they have to be copied to cache to be run from the shell.

        // Note-- this is run in a separate thread on EVERY start of the app so that updates to scripts are
        // always installed w/new versions of the app.  Also, in case the user did a clear app data from Settings.

        try {
            // first read the script file line-by-line
            InputStream is = getApplicationContext().getAssets().open(scriptName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (Exception e) {
                Log.d(TAG, "Error reading script file.  Is this a plaintext UTF-8 multi-line script?");
            }
            is.close();
            br.close();
            // next, write the string out to the file in the cache folder
            File f = new File(getCacheDir().toString() + '/' + scriptName);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes(Charset.forName("UTF-8")));
            fos.close();
            // and finally, build a queue of permission commands by replacing the filename of the
            // each SCRIPTS[] value with the matching permission command.  This lets us avoid calling
            // su -c for every time we want to chmod.  Instead we do it in one call at the end.
            SCRIPTS[index] = "chmod " + permissions + " " + f.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy: " + scriptName, e);
            return false;
        }
        return true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, String activity) {
        //Log.d("POSI", String.valueOf(position));
        // This is called from the sidemenu as callback when a item  is clickled

        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case NETHUNTER_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, NetHunterFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case KALILAUNCHER_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, KaliLauncherFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case KALISERVICES_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, KaliServicesFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case HIDE_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, HidFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case DUCKHUNTER_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, DuckHunterFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case BADUSB_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, BadusbFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case MANA_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, ManaFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case DNSMASQ_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, DnsmasqFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case IPTABLES_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, IptablesFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            case MACCHANGER_FRAGMENT:
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, MacchangerFragment.newInstance(position))
                        .addToBackStack(null)
                        .commit();
                break;
            default:
                // Start activity as usually // REMOVE THIS SOON no needed
                Intent target = new Intent();
                target.setClassName(getApplicationContext(), activity);
                startActivity(target);
                break;
        }
    }

    public void onSectionAttached(int position) {
        // restore title
        mTitle = activityNames[position];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        //Handle back button for fragments && menu
        //FragmentManager fragmentManager = getFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        }
        if (fragmentManager.getBackStackEntryCount() <= 1) {
            finish();

            return;
        }
        super.onBackPressed();
    }

    public void showMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    public String readConfigFile(String configFilePath) {


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
            Logger Logger = new Logger();
            Logger.appendLog(e.getMessage());
        }
        return text.toString();
    }

    public boolean updateConfigFile(String configFilePath, String source) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard, configFilePath);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(source);
            myOutWriter.close();
            fOut.close();
            return true;
        } catch (Exception e) {
            showMessage(e.getMessage());
            Logger Logger = new Logger();
            Logger.appendLog(e.getMessage());
            return false;
        }
    }


}

