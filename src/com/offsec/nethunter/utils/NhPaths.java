package com.offsec.nethunter.utils;

import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import static com.offsec.nethunter.AppNavHomeActivity.getAppContext;


/**********************************************
 *  IMPORTANT:   * KEEP THE APP CONSISTENT!   *
 **********************************************
 *
 *
 *   If you need to refer any path from your classes, please use this one as base,
 *
 *     Atm, all the app is using this class to get the main paths.
 *
 *
 *    You can add any path to this file but reusing the existent ones if possible.
 *
 *        Using paths in the app code:
 *
 *            all the paths in the class must start with '/' (they are all absolute)
 *            all the paths in this class must end WITHOUT "/"
 *
 *     How to use it: In the onCreate or in the onAttach you can do:
 *
 *         NhUtil nh = new NhUtil();
 *         // from the boot service (no appCTX)*
 *         NhUtil nh = new NhUtil(getFilesDir().toString());
 *
 *
 *        EX.: NhUtil nh = new NhUtil(); String myfile = nh.APP_SD_FILES_PATH + "/my/relative/route/xor.sh"
 *        EX.: NhUtil nh = new NhUtil(); String mypath = nh.APP_SD_FILES_PATH + "/my/relative/path"
 *
 *        jmingov.
 */

public class NhPaths {
    // System paths
    public final String APP_PATH;
    public String APP_INITD_PATH;
    public String APP_SCRIPTS_PATH;
    // SD Paths
    public String NH_SD_FOLDER_NAME;
    public String SD_PATH;
    public String APP_SD_FILES_PATH;
    // NetHunter paths
    private String BASE_PATH;
    public String NH_SYSTEM_PATH;
    // the chroot has this folder inside so...
    private String ARCH_FOLDER;
    // current deploy location: /data/local/nhsystem/kali-armhf
    public String CHROOT_PATH;
    // old CHROOT
    public String OLD_CHROOT_PATH;

    // constructor for app Activities and Fragments, anything with appCtx;

    public NhPaths() {
        // App base path () /data/data/com.offsec.....
        this.APP_PATH = "/data/data/com.offsec.nethunter/files";
        //this.APP_PATH = getAppContext().getFilesDir().toString();
        //final CheckForDevices UserDevice = new CheckForDevices();
        doSetup(this);
        /*  this one should be called from inside android app context
         *   (anywhere but BOOTSERVICE):
         *   nh = new NhUtil();
        */
    }
    // constructor for service (different ctx)

    public NhPaths(String _path) {
        // App base path () /data/data/com.offsec.....
        this.APP_PATH = _path;
	//final CheckForDevices UserDevice = new CheckForDevices();
        doSetup(this);
        /*  this makes the bootService not cry about getAppContext()
         *   this should be called ONLY from BOOTSERVICE like:
         *   nh = new NhUtil(getFilesDir().toString());
        */
    }

    private void doSetup(NhPaths nh) {
        // APP_PATH is now available !!!
        nh.APP_INITD_PATH = APP_PATH + "/etc/init.d";
        nh.APP_SCRIPTS_PATH = APP_PATH + "/scripts";

        // SD PATHS
        CheckForDevices UserDevice = new CheckForDevices();
        if (UserDevice.isOPO5()) {
            nh.SD_PATH = "/sdcard";
        } else {
            nh.SD_PATH = Environment.getExternalStorageDirectory().toString(); // /sdcard for the friends.
        }
        // changed from /files to /nh_files (was too generic.)
        nh.NH_SD_FOLDER_NAME = "nh_files";  // MUST MATCH assets/nh_files change both or none!!!! ^^
        nh.APP_SD_FILES_PATH = SD_PATH + "/" + NH_SD_FOLDER_NAME;
        // NetHunter paths
        nh.BASE_PATH = "/data/local";
        nh.NH_SYSTEM_PATH = BASE_PATH + "/nhsystem";
        // the chroot has this folder inside so...
        nh.ARCH_FOLDER = "/kali-armhf";
        // current deploy location: /data/local/nhsystem/kali-armhf
        nh.CHROOT_PATH = NH_SYSTEM_PATH + ARCH_FOLDER;
        // old CHROOT
        nh.OLD_CHROOT_PATH = "/data/local/kali-armhf";

    }

    public void showMessage(String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getAppContext(), message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void showMessage_long(String message) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getAppContext(), message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public String whichBusybox() {
        String[] BB_PATHS = {
                "/system/xbin/busybox_nh",
                "/sbin/busybox_nh",
                "/system/bin/busybox",
                "/data/local/bin/busybox",
                "/system/xbin/busybox",
                "/data/adb/magisk/busybox",
                "/sbin/.magisk/busybox/busybox"
        };
        for (String BB_PATH : BB_PATHS) {
            File busybox = new File(BB_PATH);
            if (busybox.exists()) {
                return BB_PATH;
            }
        }
        return "";
    }
    public String makeTermTitle(String title) {
        return "echo -ne \"\\033]0;" + title + "\\007\" && clear;";
    }
}
