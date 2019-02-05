package com.offsec.nethunter;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.offsec.nethunter.gps.KaliGPSUpdates;
import com.offsec.nethunter.gps.LocationUpdateService;
import com.offsec.nethunter.utils.CheckForRoot;
import com.winsontan520.wversionmanager.library.WVersionManager;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

public class AppNavHomeActivity extends AppCompatActivity implements KaliGPSUpdates.Provider {

    public final static String TAG = "AppNavHomeActivity";
    private static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    private static final String GPS_BACKGROUND_FRAGMENT_TAG = "BG_FRAGMENT_TAG";
    public static final String BOOT_CHANNEL_ID = "BOOT_CHANNEL";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static NavigationView navigationView;
    private CharSequence mTitle = "NetHunter";
    private final Stack<String> titles = new Stack<>();
    private static SharedPreferences prefs;
    private MenuItem lastSelected;
    private static Context c;
    private Boolean weCheckedForRoot = false;
    private Integer permsCurrent = 1;
    private boolean locationUpdatesRequested = false;
    private KaliGPSUpdates.Receiver locationUpdateReceiver;

    public static Context getAppContext() {
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // ************************************************
        c = getApplication(); //* DONT REMOVE ME *
        // ************************************************
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askMarshmallowPerms(permsCurrent);
        } else {
            CheckForRoot();
        }

        setContentView(R.layout.base_layout);

        //set kali wallpaper as background
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mDrawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation_view);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout navigationHeadView = (LinearLayout) inflater.inflate(R.layout.sidenav_header, null);
        navigationView.addHeaderView(navigationHeadView);

        FloatingActionButton readmeButton = navigationHeadView.findViewById(R.id.info_fab);
        readmeButton.setOnTouchListener((v, event) -> {
            //checkUpdate();
            showLicense();
            return false;
        });

        /// moved build info to the menu
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        prefs = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        final String buildTime = sdf.format(BuildConfig.BUILD_TIME);
        TextView buildInfo1 = navigationHeadView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = navigationHeadView.findViewById(R.id.buildinfo2);
        buildInfo1.setText(String.format("Version: %s (%s)", BuildConfig.VERSION_NAME, Build.TAGS));
        buildInfo2.setText(String.format("Built by %s at %s", BuildConfig.BUILD_NAME, buildTime));

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkTitle));
        }


//        new ShellExecuter().RunAsRootOutput("/system/bin/bootkali");
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
            ed.apply();
        }

        if (lastSelected == null) { // only in the 1st create
            lastSelected = navigationView.getMenu().getItem(0);
            lastSelected.setChecked(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setDrawerOptions();
            }
        });
        mDrawerToggle.syncState();
        // pre-set the drawer options
        setDrawerOptions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence name = getString(R.string.boot_notification_channel);
            String description = getString(R.string.boot_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(BOOT_CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }


    }

    public static void setDrawerOptions() {
        Menu menuNav = navigationView.getMenu();
        if (prefs.getBoolean(CHROOT_INSTALLED_TAG, false)) {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, true);
        } else {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, false);
        }
    }

    private void checkUpdate() {
        WVersionManager versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl("https://images.offensive-security.com/version.txt");
        versionManager.setUpdateUrl("https://images.offensive-security.com/latest.apk");
        versionManager.checkVersion();
        versionManager.setUpdateNowLabel("Update");
        versionManager.setIgnoreThisVersionLabel("Ignore");
    }

    private void showLicense() {
        // @binkybear here goes the changelog etc... \n\n%s
        String readmeData = String.format("%s\n\n%s",
                getResources().getString(R.string.licenseInfo),
                getResources().getString(R.string.nhwarning));

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("README INFO")
                .setMessage(readmeData)
                .setNegativeButton("Close", (dialog, which) -> dialog.cancel()); //nhwarning
        AlertDialog ad = adb.create();
        ad.setCancelable(false);
        ad.getWindow().getAttributes().windowAnimations = R.style.DialogStyle;
        ad.show();
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
                menuItem -> {
                    // only change it if is no the same as the last one
                    if (lastSelected != menuItem) {
                        //remove last
                        lastSelected.setChecked(false);
                        // update for the next
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
                        /*
                        case R.id.kalilauncher_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, KaliLauncherFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        */
                        case R.id.deauth_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, DeAuthFragment.newInstance(itemId))
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
                        case R.id.mpc_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, MPCFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.mitmf_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, MITMfFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.vnc_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, VNCFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.searchsploit_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, SearchSploitFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.nmap_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, NmapFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.pineapple_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, PineappleFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;

                        case R.id.gps_item:
                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, KaliGpsServiceFragment.newInstance(itemId))
                                    .addToBackStack(null)
                                    .commit();
                            break;

                        case R.id.checkforupdate_item:
                            checkUpdate();
                            break;
                    }
                    restoreActionBar();
                    return true;
                });
    }

    private void restoreActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(mTitle);

        }
    }

    private void CheckForRoot() {

        Log.d("AppNav", "Checking for Root");
        CheckForRoot mytask = new CheckForRoot(this);
        mytask.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (titles.size() > 1) {
            titles.pop();
            mTitle = titles.peek();
        }
        Menu menuNav = navigationView.getMenu();
        int i = 0;
        int mSize = menuNav.size();
        while (i < mSize) {
            if (menuNav.getItem(i).getTitle() == mTitle) {
                MenuItem _current = menuNav.getItem(i);
                if (lastSelected != _current) {
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

    private void askMarshmallowPerms(Integer permnum) {
        if (permnum == 1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            } else {
                CheckForRoot mytask = new CheckForRoot(this);
                mytask.execute();
            }
        }
        if (permnum == 2) {
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT"},
                        2);
            }
        }
        if (permnum == 3) {
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_SU")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_SU"},
                        3);
            }
        }
        if (permnum == 4) {
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_NH")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_NH"},
                        4);
            }
        }
        if (permnum == 5) {
            Log.d("HOLA", "CODE0: " + permnum);
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_NH_LOGIN")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_NH_LOGIN"},
                        5);
            }
        }
        if (permnum == 6) {
            Log.d("HOLA", "CODE0: " + permnum);
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhvnc.permission.OPEN_VNC_CONN")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhvnc.permission.OPEN_VNC_CONN"},
                        6);
            }
        }
        if (permnum == 7) {
            Log.d("HOLA", "CODE0: " + permnum);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        7);
            }
        }
        if (permnum == 8) {
            Log.d("HOLA", "CODE0: " + permnum);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        8);
            }
            // Add CheckForRoot after last permission check.  Sometimes files don't copy over on first run
            // so we need to force it to check.  Doing it earlier runs risk of no permission and SuperSU
            // display conflicting with permission requests
            CheckForRoot();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Integer permsNum = 8;
            if (permsCurrent < permsNum) {
                Log.d("AppNav", "Ask permission");
                permsCurrent = permsCurrent + 1;
                askMarshmallowPerms(permsCurrent);
            } else {
                Log.d("AppNav", "Permissions granted");
                CheckForRoot();
            }
        } else {
            Log.d("AppNav", "Not granted permission");
            askMarshmallowPerms(permsCurrent);
        }
    }

    @Override
    public void onLocationUpdatesRequested(KaliGPSUpdates.Receiver receiver) {
        locationUpdatesRequested = true;
        this.locationUpdateReceiver = receiver;
        Intent intent = new Intent(getApplicationContext(), LocationUpdateService.class);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private LocationUpdateService locationService;
    private boolean updateServiceBound = false;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to Update Service, cast the IBinder and get LocalService instance
            LocationUpdateService.ServiceBinder binder = (LocationUpdateService.ServiceBinder) service;
            locationService = binder.getService();
            updateServiceBound = true;
            if (locationUpdatesRequested) {
                locationService.requestUpdates(locationUpdateReceiver);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            updateServiceBound = false;
        }
    };


    @Override
    public void onStopRequested() {
        if (locationService != null) {
            locationService.stopUpdates();
        }
    }
}

