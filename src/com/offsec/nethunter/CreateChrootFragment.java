package com.offsec.nethunter;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.GregorianCalendar;

/**
 * Created by fattire on 3/14/15.
 * This is GPLv2'd.
 * <p/>
 * This was quickly thrown together:
 * <p/>
 * TO DO:
 * <p/>
 * * Actually verify SHA of downloaded chroot file
 * * Non-arm arch support
 * * better UI (it locks up currently when untarring/zipping file...)
 * * Add "are you sure" dialog before wiping/reinstalling
 * *  Handle situations where user opens this fragment multiple times or
 * quits during download or re-opens during download, etc.  that may
 * mean making this part of its own activity or whatever...
 * * Move strings to string resources.
 * * Clean up and make it betterer
 * * Figure why "/storage/emulated/0" -> "/storage/emulated/legacy" replacement is necessary
 * on some devices
 */


public class CreateChrootFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARCH = System.getProperty("os.arch");
    private static final String TAG = "CreateChroot";

    /* put chroot info here */

    private static final String FILENAME = "kalifs.tar.xz";
    private static final String URI = "http://3bollcdn.com/nethunter/chroot/" + FILENAME;
    //  private static final String SHA = "PUT SHA HERE"; // not yet implemented

    String zipFilePath;
    private long downloadRef;
    TextView statusText;
    String chrootPath;
    Button installButton;
    DownloadManager dm;
    BroadcastReceiver onDLFinished;
    String dir;
    final ShellExecuter x = new ShellExecuter();
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.createchroot, container, false);
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        installButton = (Button) rootView.findViewById(R.id.installButton);
        installButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onButtonHit();
            }
        });

        zipFilePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + FILENAME;

        onDLFinished = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadRef) {
                    downloadFinished();
                }
            }
        };

        getActivity().registerReceiver(onDLFinished,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return rootView;
    }

    public static CreateChrootFragment newInstance(int sectionNumber) {
        CreateChrootFragment fragment = new CreateChrootFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // which chroot?

        if (ARCH.contains("arm")) {
            dir = "kali-armhf";
        } else if (ARCH.contains("i686")) {
            dir = "kali-amd64";
        } else if (ARCH.contains("mips")) {
            dir = "kali-mips";
        } else if (ARCH.contains("x86")) {
            dir = "kali-i386";  // etc
        }
        checkForExistingChroot();
        super.onActivityCreated(savedInstanceState);
    }

    private void checkForExistingChroot() {
        // does chroot directory exist?

        chrootPath = getActivity().getFilesDir() + "/chroot/";
        statusLog("checking in chroot: " + chrootPath);
        File file = new File(chrootPath + dir + "/");

        final ShellExecuter exe = new ShellExecuter();
        String command = "if [ -d " + chrootPath + dir + " ];then echo 1; fi"; //check the dir existence
        final String _res;

        _res = exe.RunAsRootOutput(command);

        if (_res.equals("1")) {
            statusLog("An existing Kali chroot directory was found!");
            installButton.setText("Wipe Chroot");
        } else {
            statusLog("No Kali chroot directory was found.");
            file.mkdir();
            installButton.setText("Install chroot");
        }
    }

    private void onButtonHit() {
        installButton.setEnabled(false);

        String command = "if [ -d " + chrootPath + dir + " ];then echo 1; fi"; //check the dir existence
        final String _res;

        _res = x.RunAsRootOutput(command);

        if (_res.equals("1")) {
            statusLog("Deleting existing chroot...");
            removeChroot();
            checkForExistingChroot();
        } else // no chroot.  need to enable it.
            if (!startZipDownload()) {
                installButton.setEnabled(true);
            }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean startZipDownload() {

        File checkFile = new File(zipFilePath);
        if (checkFile.exists()) {
            statusLog(zipFilePath + " exists already.");
            if (checkFileIntegrity(zipFilePath)) {
                statusLog("The file looks good, so no need to re-download it.");
                inflateZip();
                return true;
            } else {
                statusLog("Deleting to make room for download...");
                if (checkFile.delete()) {
                    statusLog("Old file deleted.");
                } else {
                    statusLog("Problem deleting old file.  Just sayin'.");
                }
            }
        }
        statusLog("Starting download.  Standby...");
        if (!isExternalStorageWritable()) {
            statusLog("Nowhere to write to.  Make sure you have your external storage mounted and available.");
            return false;
        }
        dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        removeExistingDownloadOperations();
        Uri uri = Uri.parse(URI);
        DownloadManager.Request r = new DownloadManager.Request(uri);
        r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverMetered(true)
                .setTitle(FILENAME)
                .setDescription("Downloading base Kali chroot")
                .setAllowedOverRoaming(true)
                .setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, FILENAME);

        downloadRef = dm.enqueue(r);
        return true;
    }

    private void downloadFinished() {
        int status = -1;
        // what happened
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadRef);
        Cursor c = dm.query(q);
        if (c.moveToFirst()) {
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            statusLog("Download completed successfully.");
            inflateZip();
            installButton.setEnabled(true);
        } else {
            statusLog("Download failed.  Check your network connection and external storage and try again.");
            installButton.setEnabled(true);
        }
        dm.remove(downloadRef);
    }

    private void inflateZip() {
        statusLog("INFLATING...");
        // it's possible that there is a missing symlink, so check.
        try {
            x.RunAsRootWithException("ls '" + zipFilePath + '\'');
        } catch (RuntimeException ex) { // file not found
            zipFilePath = zipFilePath.replace("/storage/emulated/0", "/storage/emulated/legacy");
        }

        if (checkFileIntegrity(zipFilePath)) {
            statusLog("Extracting to chroot.  Standby...");
            // all In bg
            pd = new ProgressDialog(getActivity());
            pd.setTitle("Extracting...");
            pd.setMessage("The chroot is being unxz'd and untarred.  Depending on the speed of your device, this could take a few minutes.  If it goes more than 1/2 hour, panic.");
            pd.show();
            UnziptarTask mytask = new UnziptarTask();
            mytask.execute();


        }
    }

    private void removeChroot() {

        statusLog("Deleting Dirs...");
        new Thread(new Runnable() {
            public void run() {
                x.RunAsRootOutput("rm -rf '" + chrootPath + dir + '\'');
            }
        }).start();

    }

    private boolean checkFileIntegrity(String path) {
        statusLog("TO DO:  Check file integrity.");
        return true;
    }

    private void statusLog(String status) {
        GregorianCalendar cal = new GregorianCalendar();
        String ts = String.valueOf(cal.get(GregorianCalendar.MONTH)) + '/' +
                String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH)) + '/' +
                String.valueOf(cal.get(GregorianCalendar.YEAR)) + ' ' +
                String.valueOf(cal.get(GregorianCalendar.HOUR_OF_DAY)) + ':' +
                String.valueOf(cal.get(GregorianCalendar.MINUTE)) + ':' +
                String.valueOf(cal.get(GregorianCalendar.SECOND)) + '.' +
                String.valueOf(cal.get(GregorianCalendar.MILLISECOND)) + " - ";
        statusText.append(Html.fromHtml("<font color=\"#EDA04F\">" + ts + "</font>"));  // weird men! :)
        statusText.append(status + '\n');
    }

    private void removeExistingDownloadOperations() {
        int status;

        DownloadManager.Query q = new DownloadManager.Query();
        Cursor c = dm.query(q);
        if (c.moveToFirst()) {
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));
            dm.remove(status);
        }
    }


    public class UnziptarTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            statusLog("UNZIPPING & UNTARRING...");
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... Void) {
            try {
                // remove previous tar if it exists.
                x.RunAsRootWithException("rm -f '" + zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + '\'');
                x.RunAsRootWithException("busybox xz -d '" + zipFilePath + '\'');
                x.RunAsRootWithException("busybox tar xf '" + zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + "' -C '" + chrootPath + "'");
            } catch (RuntimeException e) {
                Log.d(TAG, "Error: ", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                statusLog("UNZIPPING & UNTARRING DONE!");
            } else {
                statusLog("There was an error :(");
            }
            pd.dismiss();
        }
    }

}

