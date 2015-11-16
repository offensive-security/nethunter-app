package com.offsec.nethunter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;

import com.winsontan520.wversionmanager.library.WVersionManager;

public class AppNavHomeActivity extends AppCompatActivity {

    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String COPY_ASSETS_TAG = "COPY_ASSETS_TAG";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private CharSequence mTitle = "NetHunter";
    private Stack<String> titles = new Stack<>();
    private SharedPreferences prefs;
    private MenuItem lastSelected;
    private NhUtil nh;
    private static Context c;

    public static Context getAppContext() {
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // ************************************************
            c = getApplication(); //* DONT REMOVE ME *
            nh = new NhUtil();
        // ************************************************

        setContentView(R.layout.base_layout);

        //set kali wallpaper as background
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout navigationHeadView = (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
        navigationView.addHeaderView(navigationHeadView);

        FloatingActionButton readmeButton = (FloatingActionButton) navigationHeadView.findViewById(R.id.info_fab);
        readmeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUpdate();
                //showLicense();
                return false;
            }
        });

        /// moved build info to the menu
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        prefs = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        String buildTime = sdf.format(BuildConfig.BUILD_TIME);
        TextView buildInfo1 = (TextView) navigationHeadView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = (TextView) navigationHeadView.findViewById(R.id.buildinfo2);
        buildInfo1.setText(String.format("Version: %s (%s)", BuildConfig.VERSION_NAME, android.os.Build.TAGS));
        buildInfo2.setText(String.format("Built by %s at %s", BuildConfig.BUILD_NAME, buildTime));

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkTitle));
        }

        // copy script files, but off the main UI thread
        final ImageView myImageView = (ImageView) findViewById(R.id.bgHome);
        File sdCardDir = new File(nh.APP_SD_FILES_PATH);
        File scriptsDir = new File(nh.APP_SCRIPTS_PATH);
        File etcDir = new File(nh.APP_INITD_PATH);
        // Copy files if: no files, no scripts, no etc or new apk version
        if (!prefs.getString(COPY_ASSETS_TAG, buildTime).equals(buildTime) || !sdCardDir.isDirectory() || !scriptsDir.isDirectory() || !etcDir.isDirectory()) {
            Log.d(COPY_ASSETS_TAG, "COPING FILES....");
            final Runnable r = new Runnable() {
                public void run() {
                    // 1:1 copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
                    assetsToFiles(nh.APP_PATH, "", "data");
                    // 1:1 copy (recursive) of the configs to  /sdcard...
                    assetsToFiles(nh.SD_PATH, "", "sdcard");
                    ShellExecuter exe = new ShellExecuter();
                    exe.RunAsRoot(new String[]{"chmod 700 " + nh.APP_SCRIPTS_PATH+"/*", "chmod 700 " + nh.APP_INITD_PATH + "/*"});
                }
            };
            Thread t = new Thread(r);
            t.start();
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString(COPY_ASSETS_TAG, buildTime);
            ed.commit();
        } else {
            Log.d(COPY_ASSETS_TAG, "FILES NOT COPIED");
        }

        // now pop in the default fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, NetHunterFragment.newInstance(R.id.nethunter_item))
                .commit();

        // and put the title in the queue for when you need to back through them
        titles.push(navigationView.getMenu().getItem(0).getTitle().toString());
        // if the nav bar hasn't been seen, let's show it
        if (!prefs.getBoolean("seenNav", false)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putBoolean("seenNav", true);
            ed.commit();
        }
        if(lastSelected == null){ // only in the 1st create
            lastSelected = navigationView.getMenu().getItem(0);
            lastSelected.setChecked(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setDrawerOptions();
                }
            }
        });
        mDrawerToggle.syncState();
        // pre-set the drawer options
        setDrawerOptions();
        checkForRoot(myImageView); //  gateway check to make sure root's possible & pop up dialog if not
    }

    public void setDrawerOptions() {
        Menu menuNav = navigationView.getMenu();
        if (prefs.getBoolean(CHROOT_INSTALLED_TAG, false)) {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, true);
        } else {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, false);
        }
    }
    public void checkUpdate(){
        WVersionManager versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl("http://images.offensive-security.com/version.txt");
        versionManager.setUpdateUrl("http://images.offensive-security.com/latest.apk");
        versionManager.checkVersion();
        versionManager.setUpdateNowLabel("Update");
        versionManager.setIgnoreThisVersionLabel("Ignore");
    }

    public void showLicense() {
        // @binkybear here goes the changelog etc... \n\n%s
        String readmeData = String.format("%s\n\n%s",
                getResources().getString(R.string.licenseInfo),
                getResources().getString(R.string.nhwarning));

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("README INFO")
                .setMessage(readmeData)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }); //nhwarning
        AlertDialog ad = adb.create();
        ad.setCancelable(false);
        ad.getWindow().getAttributes().windowAnimations = R.style.DialogStyle;

        ad.show();
    }

    public void checkForRoot(final ImageView v) {
        final AppNavHomeActivity ctx = this;
        new Thread(new Runnable() {
            public void run() {
                ShellExecuter exe = new ShellExecuter();
                final Boolean isRootAvailable = exe.isRootAvailable();
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
                        // only change it if is no the same as the last one
                        if(lastSelected != menuItem){
                            //remove last
                            lastSelected.setChecked(false);
                            // udpate for the next
                            lastSelected = menuItem;
                        }
                        //set checked
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mTitle = menuItem.getTitle();
                        titles.push(mTitle.toString());

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
                            case R.id.custom_commands_item:
                                fragmentManager
                                        .beginTransaction()
                                        .replace(R.id.container, CustomCommandsFragment.newInstance(itemId))
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
                        }
                        restoreActionBar();
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

    private Boolean pathIsAllowed(String path, String copyType) {
        // never copy images, sounds or webkit
        if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) {
            if (copyType.equals("sdcard")) {
                if (path.equals("")) {
                    return true;
                } else if (path.startsWith(nh.NH_SD_FOLDER_NAME)) {
                    return true;
                }
                return false;
            }
            if (copyType.equals("data")) {
                if (path.equals("")) {
                    return true;
                } else if (path.startsWith("scripts")) {
                    return true;
                } else if (path.startsWith("wallpapers")) {
                    return true;
                } else if (path.startsWith("etc")) {
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    // now this only copies the folders: scripts, etc , wallpapers to /data/data...
    private void assetsToFiles(String TARGET_BASE_PATH, String path, String copyType) {
        AssetManager assetManager = this.getAssets();
        String assets[];
        try {
            // Log.i("tag", "assetsTo" + copyType +"() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(TARGET_BASE_PATH, path);
            } else {
                String fullPath = TARGET_BASE_PATH + "/" + path;
                // Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && pathIsAllowed(path, copyType)) { // copy thouse dirs
                    if (!dir.mkdirs()) {
                        Log.i("tag", "could not create dir " + fullPath);
                    }
                }
                for (String asset : assets) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    if (pathIsAllowed(path, copyType)) {
                        assetsToFiles(TARGET_BASE_PATH, p + asset, copyType);
                    }
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String TARGET_BASE_PATH, String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in;
        OutputStream out;
        String newFileName = null;
        try {
            // Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            newFileName = TARGET_BASE_PATH + "/" + filename;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[8092];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of " + newFileName);
            Log.e("tag", "Exception in copyFile() " + e.toString());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (titles.size() > 1) {
            titles.pop();
            mTitle = titles.peek();
        }
        Menu menuNav = navigationView.getMenu();
        int i=0;
        int mSize = menuNav.size();
        while (i<mSize) {
            if(menuNav.getItem(i).getTitle() == mTitle){
                MenuItem _current = menuNav.getItem(i);
                if(lastSelected != _current){
                    //remove last
                    lastSelected.setChecked(false);
                    // udpate for the next
                    lastSelected = _current;
                }
                //set checked
                _current.setChecked(true);
                i = mSize;
            }
            i++;
        }
        restoreActionBar();
    }

}

