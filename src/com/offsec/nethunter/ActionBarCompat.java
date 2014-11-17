package com.offsec.nethunter;

import android.app.Activity;
import android.os.Build;

/**
 * Very limited shim for enabling the action bar's up button on devices that support it.
 */
public class ActionBarCompat {
    /**
     * This class will only ever be loaded if the version check succeeds,
     * keeping the verifier from rejecting the use of framework classes that
     * don't exist on older platform versions.
     */
    static class ActionBarCompatImpl {
        static void setDisplayHomeAsUpEnabled(Activity activity) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }



    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarCompatImpl.setDisplayHomeAsUpEnabled(activity);
        }
    }
}
