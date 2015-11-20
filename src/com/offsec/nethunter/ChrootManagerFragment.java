package com.offsec.nethunter;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    private static final String TAG = "CreateChroot";
    public static final String MIGRATE_CHROOT_TAG = "MIGRATE_CHROOT_TAG";
    public static final String DELETE_CHROOT_TAG = "DELETE_CHROOT_TAG";
    public static final String CHROOT_INSTALLED_TAG = "CHROOT_INSTALLED_TAG";
    /* put chroot info here */
    private static final String FILENAME = "kalifs-minimal.tar.xz";
    private static final String EXTRACTED_FILENAME = "kalifs-minimal.tar";
    // private static final String URI = "http://images.offensive-security.com/" + FILENAME;
    private static final String URI = "http://188.138.17.16/" + FILENAME;
    private static final String SHA512 =
            "6fe09e30236e99a9e79a9f188789944c18cedf69d6510849ac5821fad84e174c2bbfa8704a2d763" +
                    "4e39ed5aa3e5ad7e67b435642cdd733905e160fa6672bb651";


    String zipFilePath;
    String extracted_zipFilePath;
    String installLogFile;
    Boolean shouldLog = false;

    TextView statusText;
    Button installButton;
    Button updateButton;

    final ShellExecuter x = new ShellExecuter();
    ProgressDialog pd;
    SharedPreferences sharedpreferences;
    AlertDialog ad;
    NhUtil nh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nh = new NhUtil();
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
        // extracte files location

        zipFilePath = nh.SD_PATH + "/" + FILENAME;
        extracted_zipFilePath = nh.SD_PATH + "/" + EXTRACTED_FILENAME;
        installLogFile = nh.SD_PATH + "/nh_install_" + new SimpleDateFormat("yyyyMMdd_hhmmss'.log'", Locale.US).format(new Date());
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
        checkforLegacyChroot();
        super.onActivityCreated(savedInstanceState);
    }

    private void checkforLegacyChroot() {
        // does old chroot directory exist?
        if (getActivity() != null) {
            final View mView = getView();

            new Thread(new Runnable() {

                public void run() {
                    String oldchrootcheck = "if [ -d " + nh.OLD_CHROOT_PATH + " ];then echo 1; fi";  // look for old chroot
                    String newchrootcheck = "if [ -d " + nh.CHROOT_PATH + " ];then echo 1; fi"; //check the dir existence
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
            statusLog(getActivity().getString(R.string.checkingforchroot) + nh.CHROOT_PATH);
            new Thread(new Runnable() {

                public void run() {
                    String command = "if [ -d " + nh.CHROOT_PATH + " ];then echo 1; fi"; //check the dir existence
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
                                editor.commit();
                            } else {
                                // chroot not found
                                statusLog(getActivity().getString(R.string.nokalichrootfound));
                                x.RunAsRootOutput("mkdir -p " + nh.NH_SYSTEM_PATH);
                                // prevents muts 'dirty' install issue /nhsystem is nethunter property.
                                statusLog("Cleaning install directory");
                                x.RunAsRootOutput("rm -rf " + nh.NH_SYSTEM_PATH + "/*");
                                installButton.setText(getActivity().getResources().getString(R.string.installkalichrootbutton));
                                installButton.setEnabled(true);
                                updateButton.setVisibility(View.GONE);
                                editor.putBoolean(CHROOT_INSTALLED_TAG, false);
                                editor.commit();
                            }
                             // don't use apply() or it may not save
                        }
                    });

                }

            }).start();
        }
    }

    private void onButtonHit() {
        shouldLog = true;
        installButton.setEnabled(false);
        statusLog("New instalation log file: " + installLogFile);
        new Thread(new Runnable() {
            public void run() {
                String command = "if [ -d " + nh.CHROOT_PATH + " ];then echo 1; fi"; //check the dir existence
                final String _res = x.RunAsRootOutput(command);
                installButton.post(new Runnable() {
                    @Override
                    public void run() {
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
                });
            }
        }).start();
    }

    private void addMetaPackages() {
        //for now, we'll hardcode packages in the dialog view.  At some point we'll want to grab them automatically.

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle("Metapackage Install & Upgrade");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final ScrollView sv = (ScrollView) inflater.inflate(R.layout.metapackagechooser, null);
        adb.setView(sv);
        Button metapackageButton = (Button) sv.findViewById(R.id.metapackagesWeb);
        metapackageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String metapackagesURL = "http://tools.kali.org/kali-metapackages";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(metapackagesURL));
                startActivity(browserIntent);
            }
        });
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
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if(packages.equals("")){
                intent.putExtra("com.offsec.nhterm.iInitialCommand", "apt-get install " + packages);
            }
            //
            //  Log.d("PACKS:", "PACKS:" + packages);
            startActivity(intent);
        } catch (Exception e) {
            nh.showMessage(getString(R.string.toast_install_terminal));
            statusLog("Error: Terminal app not found, cant continue.");
            checkForExistingChroot();
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
                            x.RunAsRootOutput("reboot");
                        }
                    }, 4000);
        } catch (RuntimeException e) {
            Log.d(TAG, "Error: ", e);
        }
    }


    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean deleteFile(String filePath) {
        File checkFile = new File(filePath);
        if (checkFile.exists()) {
            statusLog(filePath + " found.");
            statusLog(getActivity().getString(R.string.deletingforroom));
            if (checkFile.delete()) {
                statusLog("File deleted.");
                return true;
            } else {
                statusLog(getActivity().getString(R.string.problemdeletingoldfile));
                return false;
            }
        }
        return false;
    }


    private boolean startZipDownload() {
        deleteFile(zipFilePath);
        statusLog(getActivity().getString(R.string.startingdownload));
        if (!isExternalStorageWritable()) {
            statusLog(getActivity().getString(R.string.unwritablestorageerror));
            return false;
        }
        final DownloadChroot downloadTask = new DownloadChroot(getActivity());
        downloadTask.execute(URI);
        return true;
    }

    private void inflateZip() {
        if (getActivity() != null) {
            final View mView = getView();
            pd = new ProgressDialog(getActivity());
            pd.setTitle("Checking Download... ");
            pd.setMessage("Checking file integrity...");
            pd.setCancelable(false);
            pd.show();
            statusLog("Original SHA: " + SHA512.toUpperCase());
            new Thread(new Runnable() {

                public void run() {
                    if (mView != null) {
                        final String[] checksumResponse = checkFileIntegrity(zipFilePath);
                        mView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (checksumResponse[0].equals("1")) {
                                    // all In bg
                                    pd.setTitle("Checking Download... OK");
                                    pd.setMessage("Checking file integrity... MATCH.");

                                    statusLog("New SHA: " + checksumResponse[1]);
                                    statusLog("Checking file integrity... MATCH");
                                    try {
                                        new android.os.Handler().postDelayed(
                                                new Runnable() {
                                                    public void run() {
                                                        UnziptarTask mytask = new UnziptarTask();
                                                        mytask.execute();
                                                    }
                                                }, 2000);
                                    } catch (RuntimeException e) {
                                        Log.d(TAG, "Error in start unzip: ", e);
                                    }
                                } else {
                                    pd.dismiss();
                                    // needed to add the button.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Error in the file integrity check:");
                                    builder.setMessage("Error: " + checksumResponse[1])
                                            .setNegativeButton("Abort installation", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    statusLog("Error: " + checksumResponse[1]);
                                                    pd.dismiss();
                                                    statusLog(getActivity().getString(R.string.downloadfailscheck));
                                                    checkForExistingChroot();
                                                }
                                            });

                                    ad = builder.create();
                                    ad.setCancelable(false);
                                    ad.show();
                                }
                            }
                        });
                    }

                }

            }).start();


        }

    }

    private String[] checkFileIntegrity(String path) {
        MessageDigest md;
        String newSum;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "For some reason, no SHA512 found on this device.", e);
            return new String[]{"0", "For some reason, no SHA512 found on this device."};
        }
        try {
            FileChannel fc = new FileInputStream(path).getChannel();
            /* Code from @SnakeDoc: http://stackoverflow.com/questions/16050827/filechannel-bytebuffer-and-hashing-files */
            ByteBuffer bbf = ByteBuffer.allocateDirect(8192); // allocation in bytes - 1024, 2048, 4096, 8192
            int b;
            b = fc.read(bbf);

            while ((b != -1) && (b != 0)) {

                bbf.flip();
                byte[] bytes = new byte[b];
                bbf.get(bytes);
                md.update(bytes, 0, b);
                bbf.clear();
                b = fc.read(bbf);

            }

            byte[] result = md.digest();
            newSum = String.format("%0" + (result.length * 2) + "X", new BigInteger(1, result));
            fc.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Can't read " + zipFilePath);
            return new String[]{"0", "Can't read " + zipFilePath};
        }
        // k, now check the sha.  Thanks to discussion regarding formatting at:
        // http://stackoverflow.com/questions/7166129/how-can-i-calculate-the-sha-256-hash-of-a-string-in-android
        Boolean sumpass = newSum.equalsIgnoreCase(SHA512);
        if (sumpass) {
            // match
            return new String[]{"1", newSum};
        }
        // no match
        return new String[]{"0", "BAD CHECKSUM: " + newSum};
    }

    public void doLog(String data, String fileName) {
        String[] appendToFile = {"echo '" + data + "' >> '" + fileName + "'"};
        x.RunAsRoot(appendToFile);
    }

    private void statusLog(final String status) {
        new Thread(new Runnable() {
            GregorianCalendar cal = new GregorianCalendar();
            // quick & shorter formatter
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            String ts = dateFormat.format(cal.getTime());
            String formatLog = ts + " - " + status;

            public void run() {
                if (shouldLog) {
                    doLog(formatLog, installLogFile);
                }
                statusText.post(new Runnable() {
                    @Override
                    public void run() {
                        statusText.append(Html.fromHtml("<font color=\"#EDA04F\">" + ts + " - </font>"));
                        statusText.append(status + "\n");
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


   /* --------------------------------------- asynctasks -------------------- */


    public class UnziptarTask extends AsyncTask<Void, String, Boolean> {

        Boolean isStarted = false;

        @Override
        protected void onPreExecute() {
            pd.setTitle(getActivity().getString(R.string.installing_notice));
            statusLog(getActivity().getString(R.string.unzippinganduntarring));
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progressInfo) {
            super.onProgressUpdate(progressInfo);
            if (isStarted) {
                pd.setTitle("Extracting and Deploying");
            }
            pd.setMessage(progressInfo[0]);
            statusLog(progressInfo[0]);
            isStarted = true;
        }

        @Override
        protected Boolean doInBackground(Void... Void) {
            try {
                // First: decompress .tar.xz >>> .tar
                publishProgress(getActivity().getString(R.string.extract_part1));
                x.RunAsRootWithException("busybox xz -df '" + zipFilePath + "'");
                // Second: Extract and Deploy the chroot to Destination.
                publishProgress(getActivity().getString(R.string.extract_part2));
                x.RunAsRootWithException("busybox tar -xf '" + extracted_zipFilePath + "' -C '" + nh.NH_SYSTEM_PATH + "'");
            } catch (RuntimeException e) {
                Log.d(TAG, "Error: ", e);
                publishProgress("Error: " + e.toString());
                statusLog(TAG + " >> Error: " +  e.toString());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                statusLog(getActivity().getString(R.string.unzippinguntarringdone));
                pd.setTitle("Intallation Successful.");
                pd.setMessage("Wait... loading metapackages");
                try {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    checkForExistingChroot();
                                    deleteFile(extracted_zipFilePath);
                                    pd.dismiss();
                                    addMetaPackages();
                                }
                            }, 3000);
                } catch (RuntimeException e) {
                    Log.d(TAG, "Error post extraction: ", e);
                    statusLog(TAG + " >> Error: " +  e.toString());
                }

            } else {
                statusLog(getActivity().getString(R.string.therewasanerror) + " ERROR:" + result);
                pd.dismiss();
            }

        }
    }

    private class DownloadChroot extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        ProgressDialog mProgressDialog;
        NotificationManager mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());

        private boolean isRunning = true;
        double last_perc = 0.0;
        double humanSize = 0.0;
        double onePercent = 0.0;
        double fineProgress = 0.0;

        public DownloadChroot(Context context) {
            this.context = context;
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel(true);
                }
            });
        }

        @Override
        protected void onCancelled() {
            isRunning = false;
            // mProgressDialog.setTitle("Chroot download Aborted.");
            mBuilder.setContentTitle("Chroot download Aborted.")
                    .setContentText("Download canceled by the user.")
                    .setSmallIcon(R.drawable.ic_action_perm_device_information)
                            // Removes the progress bar
                    .setProgress(0, 0, false);
            mNotifyManager.notify(1, mBuilder.build());
            statusLog("Download canceled by the user, removing temp file...");
            deleteFile(zipFilePath);
            checkForExistingChroot();
            mNotifyManager.cancel(1);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                int fileLength = connection.getContentLength();
                humanSize = fileLength / 1000000; // in MiB
                onePercent = humanSize / 100;
                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(zipFilePath);
                byte data[] = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1 && isRunning) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        fineProgress = round(round(total * 100) / fileLength);
                    publishProgress((int) (fineProgress));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mBuilder.setContentTitle("Downloading Chroot")
                    .setContentText("Starting download...")
                    .setSmallIcon(R.drawable.ic_action_refresh);
            // instantiate it within the onCreate method
            mProgressDialog.setTitle("Starting Chroot download.");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(100);
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            if (fineProgress > last_perc) {
                last_perc = fineProgress;
                double ttDownloaded = round(last_perc * onePercent);
                // if we get here, length is known, now set indeterminate to false
                // Notification
                mBuilder.setProgress(100, progress[0], false)
                        .setContentTitle("Downloading Chroot: " + last_perc + "%")
                        .setContentText("So far " + ttDownloaded + " MiB of " + humanSize + " MiB downloaded.");
                mNotifyManager.notify(1, mBuilder.build());
                // Dialog
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setProgress(progress[0]);
                mProgressDialog.setTitle("Downloading Chroot.");
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();

            if (result != null) {
                mProgressDialog.dismiss();

                mBuilder.setContentTitle("Download error.")
                        .setContentText("Error in the Chroot download.")
                        .setSmallIcon(R.drawable.ic_action_perm_device_information)
                        .setProgress(0, 0, false);
                mNotifyManager.notify(1, mBuilder.build());
                statusLog("Error in the Chroot download, posible causes: server down or conection issues");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Error in the Chroot download.");
                builder.setMessage("Error in the Chroot download, posible causes: server down or conection issues, here is the error: " + result)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                statusLog("Error in the download, removing temp file...");
                                deleteFile(zipFilePath);
                                checkforLegacyChroot();
                                mNotifyManager.cancel(1);
                            }
                        });

                ad = builder.create();
                ad.setCancelable(false);
                ad.show();
            } else {
                mProgressDialog.dismiss();
                statusLog("Chroot download completed: Total " + humanSize + " MiB.");
                mBuilder.setContentTitle("Chroot download completed.").setContentText("Chroot download completed")
                        // Removes the progress bar
                        .setProgress(0, 0, false);
                mNotifyManager.notify(1, mBuilder.build());
                inflateZip();
                mNotifyManager.cancel(1);
            }
        }

        protected double round(double value) {
            BigDecimal bd = new BigDecimal(value).setScale(1, RoundingMode.HALF_EVEN);
            value = bd.doubleValue();
            return value;
        }
    }
}
