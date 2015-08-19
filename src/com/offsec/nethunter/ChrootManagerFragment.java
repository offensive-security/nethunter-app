package com.offsec.nethunter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
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


public class ChrootManagerFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARCH = System.getProperty("os.arch");
    private static final String TAG = "CreateChroot";
    public static final String MIGRATE_CHROOT_TAG = "MIGRATE_CHROOT_TAG";
    public static final String DELETE_CHROOT_TAG = "DELETE_CHROOT_TAG";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";

    /* put chroot info here */
    private static final String FILENAME = "kalifs-minimal.tar.xz";
    private static final String URI = "http://images.offensive-security.com/" + FILENAME;
    private static final String SHA512 =
            "20e41e93ba743fad8774fe2f065a685f586acce90bf02cec570ef83865c1fc90121b89bb" +
                    "66a32e0c4f2e94e26ef339b549e8b0fa8ad104b5bc12f8251faf1330";
    private static final String OLD_CHROOT_PATH = "/data/local/kali-armhf/";

    String zipFilePath;
    private long downloadRef;
    TextView statusText;
    String chrootPath;
    Button installButton;
    Button updateButton;
    DownloadManager dm;
    BroadcastReceiver onDLFinished;
    String dir;
    final ShellExecuter x = new ShellExecuter();
    ProgressDialog pd;
    FileObserver fileObserver;
    String filesPath;
    SharedPreferences sharedpreferences;
    AlertDialog ad;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.createchroot, container, false);
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        statusText.setMovementMethod(new ScrollingMovementMethod());
        installButton = (Button) rootView.findViewById(R.id.installButton);
        installButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onButtonHit();
            }
        });
        installButton.setText("Checking...");
        updateButton = (Button) rootView.findViewById(R.id.upgradechrootbutton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addMetaPackages();
            }
        });
        updateButton.setVisibility(View.GONE);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        filesPath = "/storage/emulated/0";
        zipFilePath = filesPath + "/" + FILENAME;
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

    public static ChrootManagerFragment newInstance(int sectionNumber) {
        ChrootManagerFragment fragment = new ChrootManagerFragment();
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
        checkforLegacyChroot();
        super.onActivityCreated(savedInstanceState);
    }


    private void checkforLegacyChroot() {
        // does old chroot directory exist?
        if (getActivity() != null) {
            final View mView =  getView();

            new Thread(new Runnable() {

                public void run() {
                    String oldchrootcheck = "if [ -d " + OLD_CHROOT_PATH + " ];then echo 1; fi";  // look for old chroot
                    String newchrootcheck = "if [ -d " + chrootPath + dir + " ];then echo 1; fi"; //check the dir existence
                    final String _res = x.RunAsRootOutput(oldchrootcheck);
                    final String _res2 = x.RunAsRootOutput(newchrootcheck);

                    if (mView != null) {
                        mView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (_res.equals("1") && !_res2.equals("1")) {
                                    // old chroot but not new one
                                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                                    adb.setTitle(R.string.legacychroottitle)
                                            .setMessage(R.string.legacychrootmessage)
                                            .setPositiveButton(R.string.legacychrootposbutton, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                    startMigrateRoot();
                                                }
                                            })
                                            .setNegativeButton(R.string.legacychrootnegbutton, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog ad = adb.create();
                                    ad.setCancelable(false);
                                    ad.show();
                                } else {
                                    checkForExistingChroot();
                                }
                            }
                        });
                    }

                }

            }).start();



        }
    }

    private void startMigrateRoot() {
        installButton.setEnabled(false);
        pd = new ProgressDialog(getActivity());
        pd.setTitle(getActivity().getString(R.string.rebootingdialogtitle));
        pd.setMessage(getActivity().getString(R.string.rebootingdialogbody));
        pd.setCancelable(false);
        pd.show();
        Log.d(TAG, " PREFERENCE SET: " + MIGRATE_CHROOT_TAG);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(MIGRATE_CHROOT_TAG, MIGRATE_CHROOT_TAG);  // the full text so we can compare later
        editor.commit(); // don't use apply() or it may not save
        try {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 4s later");
                            x.RunAsRootOutput("reboot");
                        }
                    }, 4000);
        } catch (RuntimeException e) {
            Log.d(TAG, "Error: ", e);
        }
    }

    private void checkForExistingChroot() {

        // does chroot directory exist?
        if (getActivity() != null) {
            chrootPath = getActivity().getFilesDir() + "/chroot/";
            statusLog(getActivity().getString(R.string.checkingforchroot) + chrootPath);
            new Thread(new Runnable() {

                public void run() {
                    String command = "if [ -d " + chrootPath + dir + " ];then echo 1; fi"; //check the dir existence
                    final String _res = x.RunAsRootOutput(command);



                    installButton.post(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            if (_res.equals("1")) {
                                statusLog(getActivity().getString(R.string.existingchrootfound));
                                installButton.setText(getActivity().getResources().getString(R.string.removekalichrootbutton));
                                installButton.setEnabled(true);
                                updateButton.setVisibility(View.VISIBLE);
                                editor.putBoolean(CHROOT_INSTALLED_TAG, true);
                            } else {
                                File file = new File(chrootPath + "/");
                                statusLog(getActivity().getString(R.string.nokalichrootfound));
                                file.mkdir();
                                installButton.setText(getActivity().getResources().getString(R.string.installkalichrootbutton));
                                installButton.setEnabled(true);
                                updateButton.setVisibility(View.GONE);
                                editor.putBoolean(CHROOT_INSTALLED_TAG, false);
                            }

                            editor.commit(); // don't use apply() or it may not save

                        }
                    });

                }

            }).start();




        }
    }

    private void onButtonHit() {
        installButton.setEnabled(false);
        String command = "if [ -d " + chrootPath + dir + " ];then echo 1; fi"; //check the dir existence
        final String _res;

        _res = x.RunAsRootOutput(command);

        if (_res.equals("1")) {
            // the chroot is there.
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(getActivity().getString(R.string.reallyremovechroot));
            adb.setMessage(getActivity().getString(R.string.nogoingback));
            adb.setPositiveButton(getActivity().getString(R.string.rebootbutton), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reallyWipeRoot();
                }
            });
            adb.setNegativeButton(getActivity().getString(R.string.chickenoutbutton), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installButton.setEnabled(true);
                }
            });
            AlertDialog ad = adb.create();
            ad.setCancelable(false);
            ad.show();
        } else {
            // no chroot.  need to enable it.
            if (!startZipDownload()) {
                installButton.setEnabled(true);
            }
        }
    }

    private void addMetaPackages() {
        //for now, we'll hardcode packages in the dialog view.  At some point we'll want to grab them automatically.

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Metapackage Install & Upgrade");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final ScrollView sv = (ScrollView) inflater.inflate(R.layout.metapackagechooser, null);
        adb.setView(sv);
        WebView wv = (WebView) sv.findViewById(R.id.metapackagesWebView);
        wv.loadUrl("https://www.kali.org/news/kali-linux-metapackages/");
        adb.setPositiveButton(R.string.InstallAndUpdateButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder sb = new StringBuilder("");
                CheckBox cb;
                // now grab all the checkboxes in the dialog and check their status
                // thanks to "user2" for a 2-line sample of how to get the dialog's view:  http://stackoverflow.com/a/13959585/3035127 
                AlertDialog d = AlertDialog.class.cast(dialog);
                LinearLayout ll = (LinearLayout) d.findViewById(R.id.metapackageLinearLayout);
                int children = ll.getChildCount();
                for (int cnt = 0; cnt < children; cnt++) {
                    if (ll.getChildAt(cnt) instanceof CheckBox) {
                        cb = (CheckBox) ll.getChildAt(cnt);
                        if (cb.isChecked()) {
                            sb.append(cb.getText()).append(" ");
                        }
                    }
                }
                installAndUpgrade(sb.toString());
            }
        });
        ad = adb.create();
        ad.setCancelable(true);
        ad.show();
    }

    private void installAndUpgrade(String packages) {

        try {
            Intent intent =
                    new Intent("jackpal.androidterm.RUN_SCRIPT");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("jackpal.androidterm.iInitialCommand", "su -c '" + getActivity().getFilesDir() + "/scripts/bootkali apt-get install " + packages + "'");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=jackpal.androidterm")));
            } catch (android.content.ActivityNotFoundException anfe2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jackpal.androidterm")));
            }
        }
    }

    private void reallyWipeRoot() {
        installButton.setEnabled(false);
        pd = new ProgressDialog(getActivity());
        pd.setTitle(getActivity().getString(R.string.rebootingdialogtitle));
        pd.setMessage(getActivity().getString(R.string.rebootingdialogbody));
        pd.setCancelable(false);
        pd.show();
        Log.d(TAG, " PREFERENCE SET: " + DELETE_CHROOT_TAG);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(DELETE_CHROOT_TAG, DELETE_CHROOT_TAG);  // the full text so we can compare later
        editor.commit(); // don't use apply() or it may not save
        try {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 4s later");
                            x.RunAsRootOutput("reboot");
                        }
                    }, 4000);

        } catch (RuntimeException e) {
            Log.d(TAG, "Error: ", e);
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean startZipDownload() {


        File checkFile = new File(zipFilePath);
        File otherFile = new File(zipFilePath.replace("/storage/emulated/0", "/storage/emulated/legacy"));
        if (checkFile.exists() || otherFile.exists()) {

            statusLog(zipFilePath + getActivity().getString(R.string.existsalready));
            if (checkFileIntegrity(zipFilePath) || checkFileIntegrity(zipFilePath.replace("/storage/emulated/0", "/storage/emulated/legacy"))) {
                statusLog(getActivity().getString(R.string.filelooksgood));
                inflateZip();
                return true;
            } else {
                statusLog(getActivity().getString(R.string.deletingforroom));
                if (checkFile.delete()) {
                    statusLog(getActivity().getString(R.string.oldfiledeleted));
                } else {
                    statusLog(getActivity().getString(R.string.problemdeletingoldfile));
                }
            }
        }
        statusLog(getActivity().getString(R.string.startingdownload));

        if (!isExternalStorageWritable()) {
            statusLog(getActivity().getString(R.string.unwritablestorageerror));
            return false;
        }
        final String micmd = "wget "+ URI + " -P " + filesPath;



        final View mView =  getView();
        new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "wget STARTING ::: " + micmd);
                final String res = x.RunAsRootOutput(micmd);

                if (mView != null) {
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "wget OUT ::: " + res);
                            downloadFinished();
                        }
                    });
                }
            }

        }).start();

        /*
        dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        removeExistingDownloadOperations();
        Uri uri = Uri.parse(URI);

        DownloadManager.Request r = new DownloadManager.Request(uri);
        r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverMetered(true)
                .setTitle(FILENAME)
                .setDescription(getActivity().getString(R.string.downloadingdescription))
                .setAllowedOverRoaming(true)
                .setDestinationUri(Uri.parse("file://" + zipFilePath + ".partial"));

        downloadRef = dm.enqueue(r);
        // start watching download progress
        fileObserver = new DownloadsObserver(filesPath);
        fileObserver.startWatching();
        */
        return true;
    }

    private void downloadFinished() {
      /*  int status = -1;
        // what happened
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadRef);
        Cursor c = dm.query(q);
        if (c.moveToFirst()) {
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }
        // close the cursor
        c.close();
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            // everything ok so we dont longer need the fileobserver
            fileObserver.stopWatching();
            pd.dismiss();*/

            // remove the partial...in the bg
            final View mView =  getView();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        x.RunAsRoot("mv " + zipFilePath + ".partial " + zipFilePath);
                    } catch (RuntimeException ex) { // file not found
                        zipFilePath = zipFilePath.replace("/storage/emulated/0", "/storage/emulated/legacy");
                        try {
                            x.RunAsRoot("mv " + zipFilePath + ".partial " + zipFilePath);
                        } catch (RuntimeException e) {
                            statusLog(getActivity().getString(R.string.downloaderrormissingfile) + e);
                        }
                    }

                    if (mView != null) {
                        mView.post(new Runnable() {
                            @Override
                            public void run() {
                                statusLog(getActivity().getString(R.string.downloadsuccessful));
                                inflateZip();
                            }
                        });
                    }
                }

            }).start();

        /*} else {
            statusLog(getActivity().getString(R.string.downloadfailedtryagain));
            installButton.setEnabled(true);
        }
        dm.remove(downloadRef);*/
    }

    private void inflateZip() {
        statusLog(getActivity().getString(R.string.inflating));

        // look for bad path again...in the bg
        new Thread(new Runnable() {
            public void run() {
                try {
                    x.RunAsRoot("ls " + zipFilePath);
                } catch (RuntimeException ex) { // file not found
                    zipFilePath = zipFilePath.replace("/storage/emulated/0", "/storage/emulated/legacy");
                    try {
                        x.RunAsRoot("ls " + zipFilePath);
                    } catch (RuntimeException e) {
                        statusLog(getActivity().getString(R.string.couldntfindfile) + e);
                    }
                }
            }

        }).start();


        if (checkFileIntegrity(zipFilePath)) {
            statusLog(getActivity().getString(R.string.extractinglogtext));
            // all In bg
            pd = new ProgressDialog(getActivity());
            pd.setTitle(getActivity().getString(R.string.extractingdialogtitle));
            pd.setCancelable(false);
            pd.setMessage(getActivity().getString(R.string.extractingdialogmessage));
            pd.show();
            UnziptarTask mytask = new UnziptarTask();
            mytask.execute();
        } else {
            statusLog(getActivity().getString(R.string.downloadfailscheck));
            checkForExistingChroot();
        }
    }

    private boolean checkFileIntegrity(String path) {

        File theFile = new File(path);
        byte[] bytes = new byte[(int) theFile.length()];
        DataInputStream dis;
        MessageDigest md;
        try {
            dis = new DataInputStream(new FileInputStream(theFile));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error:  Can't find " + zipFilePath, e);
            return false;
        }
        try {
            dis.readFully(bytes);
        } catch (NullPointerException | IOException e) {
            Log.e(TAG, "Error reading " + zipFilePath, e);
            return false;
        }
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "For some reason, no SHA512 found on this device.", e);
            return false;
        }
        md.reset();

        // k, now check the sha.  Thanks to discussion regarding formatting at:
        // http://stackoverflow.com/questions/7166129/how-can-i-calculate-the-sha-256-hash-of-a-string-in-android
        byte[] result = md.digest(bytes);
        return String.format("%0" + (result.length * 2) + "X", new BigInteger(1, result)).
                equalsIgnoreCase(SHA512);
    }

    private void statusLog(String status) {
        GregorianCalendar cal = new GregorianCalendar();
        // quick & shorter formatter
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String ts = dateFormat.format(cal.getTime());
        statusText.append(Html.fromHtml("<font color=\"#EDA04F\">" + ts + " - </font>"));
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
        // close the cursor
        c.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


   /* --------------------------------------- asynctasks -------------------- */


    public class UnziptarTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            statusLog(getActivity().getString(R.string.unzippinganduntarring));
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... Void) {
            try {
                x.RunAsRoot("busybox xz -df '" + zipFilePath + "';" +
                        "busybox tar xf '" + zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + "' -C '" + chrootPath + "'");
            } catch (RuntimeException e) {
                Log.d(TAG, "Error: ", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                statusLog(getActivity().getString(R.string.unzippinguntarringdone));
            } else {
                statusLog(getActivity().getString(R.string.therewasanerror));
            }
            pd.dismiss();
            checkForExistingChroot();
            addMetaPackages();
        }
    }

    public class DownloadsObserver extends FileObserver {

        int upc = 0;
        double last_progress = 0;
        private static final int flags = FileObserver.MODIFY;

        public DownloadsObserver(String path) {
            super(path, flags);
            pd = new ProgressDialog(getActivity());
            pd.setTitle(getActivity().getString(R.string.downloadingchroot));
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMessage(getActivity().getString(R.string.downloadingstandby));
            pd.show();
        }

        @Override
        public void onEvent(int event, String path) {
            upc = upc + 1;

            if (path != null && event == FileObserver.MODIFY) {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadRef);
                Cursor c = dm.query(q);
                if (c.moveToFirst()) {
                    int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    long size = c.getInt(sizeIndex);
                    long downloaded = c.getInt(downloadedIndex);
                    double progress = 0.0;

                    if (size != -1) {
                        progress = Math.round(downloaded * 100.0 / size);
                    }
                    // this ev is launched each ~2ms,only update the progress if is a 'big one' > 1% (also de downloadProgres doesnt support doubles)
                    if (last_progress < progress) {
                        last_progress = progress;
                        pd.setProgress((int) progress);
                    }
                }
                c.close();

            }
        }
    }
}

