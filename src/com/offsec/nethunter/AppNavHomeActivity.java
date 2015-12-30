package com.offsec.nethunter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;

import com.offsec.nethunter.utils.CheckForRoot;
import com.winsontan520.wversionmanager.library.WVersionManager;

public class AppNavHomeActivity extends AppCompatActivity {

    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";

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
    private static Context c;
    private Boolean weCheckedForRoot = false;
    private final String BuildUser = "Kali";  // Change this to your name/username
    private Integer permsNum = 6;
    private Integer permsCurrent = 1;
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
            CheckForRoot mytask = new CheckForRoot(this);
            mytask.execute();
        }


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
                //checkUpdate();
                showLicense();
                return false;
            }
        });

        /// moved build info to the menu
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a zzz",
                Locale.US);

        prefs = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        final String buildTime = sdf.format(BuildConfig.BUILD_TIME);
        TextView buildInfo1 = (TextView) navigationHeadView.findViewById(R.id.buildinfo1);
        TextView buildInfo2 = (TextView) navigationHeadView.findViewById(R.id.buildinfo2);
        buildInfo1.setText(String.format("Version: %s (%s)", BuildConfig.VERSION_NAME, Build.TAGS));
        buildInfo2.setText(String.format("Built by %s at %s", BuildUser, buildTime));

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
        versionManager.setVersionContentUrl("https://images.offensive-security.com/version.txt");
        versionManager.setUpdateUrl("https://images.offensive-security.com/latest.apk");
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
                            case R.id.checkforupdate_item:
                                checkUpdate();
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
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(mTitle);

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
    private void askMarshmallowPerms(Integer permnum){
        if(permnum == 1){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        if(permnum == 2){
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT"},
                        2);
            }}
        if(permnum == 3){
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_SU")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_SU"},
                        3);
            }}
        if(permnum == 4){
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_NH")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_NH"},
                        4);
            }
        }
        if(permnum == 5){
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhterm.permission.RUN_SCRIPT_NH_LOGIN")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhterm.permission.RUN_SCRIPT_NH_LOGIN"},
                        5);
            }
        }
        if(permnum == 6) {
            if (ContextCompat.checkSelfPermission(this,
                    "com.offsec.nhvnc.permission.OPEN_VNC_CONN")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.offsec.nhvnc.permission.OPEN_VNC_CONN"},
                        6);
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(permsCurrent < permsNum){
                        permsCurrent = permsCurrent+1;
                        askMarshmallowPerms(permsCurrent);
                    } else {
                        CheckForRoot mytask = new CheckForRoot(this);
                        mytask.execute();
                    }
                } else {
                    askMarshmallowPerms(permsCurrent);
                }
    }
}

