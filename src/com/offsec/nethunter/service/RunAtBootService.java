package com.offsec.nethunter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.offsec.nethunter.ChrootManagerFragment;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.R;
import com.offsec.nethunter.utils.ShellExecuter;


public class RunAtBootService extends Service {
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    public static final String TAG = "Nethunter: Startup";
    final ShellExecuter x = new ShellExecuter();
    SharedPreferences sharedpreferences;
    NhPaths nh;
    Boolean runBootServices = true;
    String doing_action = "";
    Notification.Builder n = null;
    public RunAtBootService() {
    }

    private Notification.Builder doNotification(String title, String contents){
                if(n == null){
                    n  = new Notification.Builder(this);
                }
                n.setStyle(new Notification.BigTextStyle().bigText(contents))
                        .setContentTitle(title)
                //.setContentText(contents)
                .setSmallIcon(R.drawable.ic_stat_ic_nh_notificaiton)
                // .setContentIntent(pIntent)
                .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(999, n.build());
        return n;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doNotification(TAG, "Doing boot checks");
        nh = new NhPaths(getFilesDir().toString());
        sharedpreferences = getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        // NOTE:  If the Nethunter app has not yet been run (to install these files), this won't do
        // anything.  For that reason it may be wise to do a full install of the files at boot as
        // well, but that doesn't happen now.  Easy to add, but merits some discussion if script
        // updates should be done at boot, at every app start (current practice), etc.
        if (!sharedpreferences.getBoolean(CHROOT_INSTALLED_TAG, false)) {
            // chroot not installed
            Log.d(TAG,"Nethunter chroot is not installed.");
            doing_action = "Nethunter chroot is not installed.";
            runBootServices = false;
            doNotification(TAG, getString(R.string.nokalichrootfound));
        }
        // check for DELETE_CHROOT_TAG pref & make sure default is NO
        if(ChrootManagerFragment.DELETE_CHROOT_TAG.equals(sharedpreferences.getString(ChrootManagerFragment.DELETE_CHROOT_TAG, ""))){
            doing_action = "DELETE CHROOT";
            doNotification(TAG, "Delete chroot request found.");
            runBootServices = false;
            // DELETE IS IN THE QUEUE -->  CHECK IF WE ARE UNMOUNTED BY COUNTING REFERENCES TO "KALI"
            // IN /proc/mounts
            String command = "if [ $(grep kali /proc/mounts -c) -ne 0 ];then echo 1; fi"; //check cmd
            final String _res;

            _res = x.RunAsRootOutput(command);

            if (_res.equals("1")) {
                Toast.makeText(getBaseContext(), getString(R.string.toastchrootmountedwarning), Toast.LENGTH_LONG).show();
                doNotification(TAG, getString(R.string.toastchrootmountedwarning));
            } else{
                doNotification(TAG, getString(R.string.toastdeletingchroot));
                Toast.makeText(getBaseContext(), getString(R.string.toastdeletingchroot), Toast.LENGTH_LONG).show();
                x.RunAsRootOutput("su -c 'rm -rf " + nh.NH_SYSTEM_PATH + "/*'");
                // remove the sp so we dont remove it again on next boot
                sharedpreferences.edit().remove(ChrootManagerFragment.DELETE_CHROOT_TAG).apply();
                sharedpreferences.edit().remove(ChrootManagerFragment.CHROOT_INSTALLED_TAG).apply();

                Toast.makeText(getBaseContext(), getString(R.string.toastdeletedchroot), Toast.LENGTH_LONG).show();
                doNotification(TAG, getString(R.string.toastdeletedchroot));

            }

        }

        if(ChrootManagerFragment.MIGRATE_CHROOT_TAG.equals(sharedpreferences.getString(ChrootManagerFragment.MIGRATE_CHROOT_TAG, ""))) {
            doing_action = "MIGRATE CHROOT";
            doNotification(TAG, "Migrate chroot request found.");
            runBootServices = false;
            // CHECK IF WE ARE UNMOUNTED BY COUNTING REFERENCES TO "KALI"
            // IN /proc/mounts
            String command = "if [ $(grep kali /proc/mounts -c) -ne 0 ];then echo 1; fi"; //check cmd
            final String _res;

            _res = x.RunAsRootOutput(command);

            if (_res.equals("1")) {
                Toast.makeText(getBaseContext(), getString(R.string.toastchrootmountedwarning), Toast.LENGTH_LONG).show();
                doNotification(TAG, getString(R.string.toastchrootmountedwarning));
            } else {
                doNotification(TAG, "Starting chroot migration...");
                Toast.makeText(getBaseContext(), getString(R.string.toastmigratingchroot), Toast.LENGTH_LONG).show();
                x.RunAsRootOutput("su -c 'mv " + nh.OLD_CHROOT_PATH + " " + nh.NH_SYSTEM_PATH + "'");
                Toast.makeText(getBaseContext(), getString(R.string.toastmigratedchroot), Toast.LENGTH_LONG).show();
                sharedpreferences.edit().remove(ChrootManagerFragment.MIGRATE_CHROOT_TAG).apply();
                doNotification(TAG, getString(R.string.toastmigratedchroot));
            }
        }

        if (userinit(runBootServices)) {
            Toast.makeText(getBaseContext(), "Boot end: ALL OK", Toast.LENGTH_SHORT).show();
            doNotification(TAG, "Boot ended. All fine. Action performed: " + doing_action + " OK");
        } else {
            if(!runBootServices){
                Toast.makeText(getBaseContext(), "Not runing boot scripts. OK", Toast.LENGTH_SHORT).show();
                doNotification(TAG, "Boot ended. All fine. Action performed: " + doing_action + " OK");
            } else {
                doNotification(TAG, "Boot ended. No busybox found!");
            }
        }
        // put change MAC addresses here.
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // don't support binding for now.
    }

    public boolean userinit(Boolean ShouldRun) {
        if(!ShouldRun){
            return false;
        }
        doing_action = "RUNNING BOOT SERVICES";
        doNotification(TAG, "RUNNING BOOT SERVICES");
        // this duplicates the functionality of the userinit service, formerly in init.rc
        // These scripts will start up after the system is booted.
        // Put scripts in fileDir/scripts/etc/init.d/ and set execute permission.  Scripts should
        // start with a number and include a hashbang such as #!/system/bin/sh as the first line.
        ShellExecuter exe = new ShellExecuter();
        String busybox = nh.whichBusybox();
        if(busybox != null){
            String[] runner = {busybox + " run-parts " +  nh.APP_INITD_PATH};
            exe.RunAsRoot(runner);
            Toast.makeText(getBaseContext(), getString(R.string.autorunningscripts), Toast.LENGTH_SHORT).show();
            return true;
        }
        Toast.makeText(getBaseContext(), getString(R.string.toastForNoBusybox), Toast.LENGTH_SHORT).show();
        doNotification(TAG, getString(R.string.toastForNoBusybox));
        return false;
    }
}
