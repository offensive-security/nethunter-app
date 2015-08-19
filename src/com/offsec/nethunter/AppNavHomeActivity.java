package com.offsec.nethunter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Stack;

import eu.chainfire.libsuperuser.Shell;

public class AppNavHomeActivity extends AppCompatActivity {

    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";

    // these must be UTF-8 text-based scripts.  They go in the /assets folder and will be copied
    // to the app's private file area.  Don't make thisstatic or final because it changes below (is reused)
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

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private CharSequence mTitle = "NetHunter";
    private Stack<String> titles = new Stack<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        //set kali wallpaper as background
        //String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/kali-nh/wallpaper/kali-nh-2183x1200.png";
        AssetManager assetManager = getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open("wallpapers/kali-nh-2183x1200.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        ImageView myImageView = (ImageView) findViewById(R.id.bgHome);
        myImageView.setImageBitmap(bitmap);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));
        }

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
        createDirIfNeeded(getFilesDir() + "/scripts/etc/init.d");

        // now pop in the default fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, NetHunterFragment.newInstance(R.id.nethunter_item))
                .commit();

        // and put the title in the queue for when you need to back through them
        titles.push(mTitle.toString());

        prefs = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        // make sure we check if we have chroot every time we open the drawer, so that
        // we can disable everything but the Chroot Manager
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // might wanna put this in ondrawerslide() above so that things
                // don't disappear once the drawer is open, but anyhoo...
                setDrawerOptions();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

        });

        // if the nav bar hasn't been seen, let's show it
        if (!prefs.getBoolean("seenNav", false)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putBoolean("seenNav", true);
            ed.commit();
        }

        // pre-set the drawer options
        setDrawerOptions();
        checkForRoot(myImageView); //  gateway check to make sure root's possible & pop up dialog if not in the bg
    }

    public void checkForRoot(final View v) {
        final AppNavHomeActivity ctx = this;
        new Thread(new Runnable() {
            public void run() {

                final Boolean isRootAvailable = Shell.SU.available();
                v.post(new Runnable() {
                    @Override
                    public void run() {

                        if (!isRootAvailable) {
                            AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
                            adb.setTitle(R.string.rootdialogtitle)
                                    .setMessage(R.string.rootdialogmessage)
                                    .setPositiveButton(R.string.rootdialogposbutton, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            checkForRoot(v);
                                        }
                                    })
                                    .setNegativeButton(R.string.rootdialognegbutton, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                            AlertDialog ad = adb.create();
                            ad.setCancelable(false);
                            ad.show();
                        }
                    }


                });
            }
        }).start();


    }

    /* if the chroot isn't set up, don't show the chroot options */

    private void setDrawerOptions() {
        Menu menuNav = navigationView.getMenu();
        if (prefs.getBoolean(CHROOT_INSTALLED_TAG, false)) {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, true);
        } else {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, false);
        }
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
            // next, write the string out to the file in the files folder
            File f = new File(getFilesDir().toString() + "/scripts/" + scriptName);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mTitle = menuItem.getTitle();

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        int itemId = menuItem.getItemId();
                        switch (itemId) {
                            case R.id.nethunter_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, NetHunterFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.kalilauncher_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, KaliLauncherFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.kaliservices_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, KaliServicesFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.hid_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, HidFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.duckhunter_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, DuckHunterFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.badusb_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, BadusbFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.mana_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, ManaFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.dnsmasq_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, DnsmasqFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.iptables_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, IptablesFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.macchanger_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, MacchangerFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.createchroot_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, ChrootManagerFragment.newInstance(itemId))
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            default:
                                // Start activity as usually // REMOVE THIS SOON not needed
                                Intent target = new Intent();
                                target.setClassName(getApplicationContext(), "AppNavHomeActivity");
                                startActivity(target);
                                break;
                        }
                        restoreActionBar();
                        titles.push(mTitle.toString());
                        menuItem.setChecked(true);
                        return true;
                    }
                });
    }


    public void restoreActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            //  ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(mTitle);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (titles.size() > 1) {
            titles.pop();
            mTitle = titles.peek();
        }
        restoreActionBar();
    }
}

