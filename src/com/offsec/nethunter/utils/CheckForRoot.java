package com.offsec.nethunter.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.offsec.nethunter.R;



public class CheckForRoot  extends AsyncTask<String, Boolean, String> {

    private final String TAG = "CHECK_FOR_ROOT";
    private final Context ctx;
    private final ShellExecuter exe;

    private ProgressDialog pd;
    private Boolean isRootAvailable;
    public CheckForRoot(Context _ctx){
        Log.d(TAG, "CONTRUCTING");
        this.ctx = _ctx;

        exe = new ShellExecuter();
        isRootAvailable = false;

    }

    @Override
    protected String doInBackground(String... data) {
            isRootAvailable = exe.isRootAvailable();
            publishProgress(isRootAvailable);
            return "CHECK FOR ROOT DONE: " + isRootAvailable;
    }
    @Override
    protected void onProgressUpdate(Boolean... progress) {
        if(progress[0]){
            cancel(true);
            CopyBootFiles mytask = new CopyBootFiles(ctx);
            mytask.execute();

        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
            adb.setTitle(R.string.rootdialogtitle)
                    .setMessage(R.string.rootdialogmessage)
                    .setPositiveButton(R.string.rootdialogposbutton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            cancel(true);
                            CheckForRoot mytask = new CheckForRoot(ctx);
                            mytask.execute();
                        }
                    })
                    .setNegativeButton(R.string.rootdialognegbutton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(true);
                            ((Activity) ctx).finish();
                        }
                    });
            AlertDialog ad = adb.create();
            ad.setCancelable(false);
            ad.show();
        }
    }
    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, result);
    }


}
