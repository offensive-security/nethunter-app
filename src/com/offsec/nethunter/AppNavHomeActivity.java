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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Stack;

public class AppNavHomeActivity extends AppCompatActivity {

    public final static String TAG = "AppNavHomeActivity";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String COPY_ASSETS_TAG = "COPY_ASSETS_TAG";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private CharSequence mTitle = "NetHunter";
    private Stack<String> titles = new Stack<>();
    private SharedPreferences prefs;
    String filesPath;
    String sdCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        //set kali wallpaper as background

        filesPath = getFilesDir().toString();
        sdCard = Environment.getExternalStorageDirectory().toString();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        prefs = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        File sdCardDir = new File(Environment.getExternalStorageDirectory() + "/files");
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
        final ImageView myImageView = (ImageView) findViewById(R.id.bgHome);
        if (!prefs.getBoolean(COPY_ASSETS_TAG, false) || !sdCardDir.isDirectory()) {

            Log.d(COPY_ASSETS_TAG,"COPING FILES....");
            final Runnable r = new Runnable() {
                public void run() {
                    // 1:1 copy (recursive) of the assets/{scripts, etc, wallpapers} folders to /data/data/...
                    assetsToFiles(filesPath, "", "data");
                    // 1:1 copy (recursive) of the configs to  /sdcard...
                    assetsToFiles(sdCard, "", "sdcard");
                    ShellExecuter exe = new ShellExecuter();
                    exe.RunAsRoot(new String[]{"chmod 700 " + getFilesDir() + "/scripts/*", "chmod 700 " + getFilesDir() + "/etc/init.d/*"});


                    myImageView.post(new Runnable() {
                        @Override
                        public void run() {

                            String imageInSD = filesPath + "/wallpapers/kali-nh-2183x1200.png";
                            Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);

                            myImageView.setImageBitmap(bitmap);
                        }


                    });
                }
            };
            Thread t = new Thread(r);
            t.start();
            SharedPreferences.Editor ed = prefs.edit();
            ed.putBoolean(COPY_ASSETS_TAG, true);
            ed.commit();
        } else {
            Log.d(COPY_ASSETS_TAG,"FILES NOT COPIED");
            String imageInSD = filesPath + "/wallpapers/kali-nh-2183x1200.png";
            Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
            myImageView.setImageBitmap(bitmap);
        }

        // now pop in the default fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, NetHunterFragment.newInstance(R.id.nethunter_item))
                .commit();

        // and put the title in the queue for when you need to back through them
        titles.push(mTitle.toString());



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
        checkForRoot(myImageView); //  gateway check to make sure root's possible & pop up dialog if not
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

    private void setDrawerOptions() {
        Menu menuNav = navigationView.getMenu();
        if (prefs.getBoolean(CHROOT_INSTALLED_TAG, false)) {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, true);
        } else {
            menuNav.setGroupEnabled(R.id.chrootDependentGroup, false);
        }
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
    private Boolean pathIsAllowed(String path, String copyType) {
        // never copy images, sounds or webkit
        if(!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) {
            if (copyType.equals("sdcard")) {
                if(path.equals("") || path.startsWith("files")) {
                    return true;
                }
                return false;
            }
            if (copyType.equals("data")) {
                if(path.equals("") || path.startsWith("scripts") || path.startsWith("wallpapers") || path.startsWith("etc")) {
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
                String fullPath =  TARGET_BASE_PATH + "/" + path;
                // Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && pathIsAllowed(path, copyType)) { // copy thouse dirs
                    if (!dir.mkdirs()){
                        Log.i("tag", "could not create dir " + fullPath);
                    }
                }
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }
                    if (pathIsAllowed(path, copyType)) {
                        assetsToFiles(TARGET_BASE_PATH, p + assets[i], copyType);
                    }
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }
    private void copyFile(String TARGET_BASE_PATH, String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            // Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            newFileName = TARGET_BASE_PATH + "/" + filename;
            File nfile = new File(newFileName);
            // only if the file isnt there.
            if(!nfile.exists()) {
                out = new FileOutputStream(newFileName);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of "+newFileName);
            Log.e("tag", "Exception in copyFile() "+e.toString());
        }

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

