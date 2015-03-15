package com.offsec.nethunter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.offsec.nethunter.R;
import com.offsec.nethunter.ShellExecuter;

import java.io.File;

public class RunAtBootService extends Service {

    public static final String TAG = "NH: RunAtBootService";

    public RunAtBootService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // NOTE:  If the Nethunter app has not yet been run (to install these files), this won't do
        // anything.  For that reason it may be wise to do a full install of the files at boot as
        // well, but that doesn't happen now.  Easy to add, but merits some discussion if script
        // updates should be done at boot, at every app start (current practice), etc.

        if (userinit()) {
            Log.d(TAG, "ran scripts successfully.");
        }
        // put change MAC addresses here.
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // don't support binding for now.
    }

    public boolean userinit() {

        // this duplicates the functionality of the userinit service, formerly in init.rc
        // These scripts will start up after the system is booted.
        // Put scripts in fileDir/etc/init.d/ and set execute permission.  Scripts should
        // start with a number and include a hashbang such as #!/system/bin/sh as the first line.

        File busybox = new File("/system/xbin/busybox");
        if (!busybox.exists()) {
            busybox = new File("/data/local/bin/busybox");
            if (!busybox.exists()) {
                busybox = new File("/system/bin/busybox");
                if (!busybox.exists()) {
                    Log.d(TAG, "Busybox not found.");
                    Toast.makeText(getBaseContext(), getString(R.string.toastForNoBusybox), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        ShellExecuter exe = new ShellExecuter();
        String[] runner = {busybox.getAbsolutePath() + " run-parts " + getFilesDir() + "/etc/init.d"};
        Log.d(TAG, "executing: " + runner[0]);
        Toast.makeText(getBaseContext(), getString(R.string.autorunningscripts), Toast.LENGTH_SHORT).show();
        exe.RunAsRoot(runner);
        return true;
    }
}