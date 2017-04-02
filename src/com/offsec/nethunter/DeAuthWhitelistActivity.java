package com.offsec.nethunter;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.Locale;

public class DeAuthWhitelistActivity extends AppCompatActivity {

    private NhPaths nh;
    private final ShellExecuter exe = new ShellExecuter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nh = new NhPaths();
        setContentView(R.layout.deauth_whitelist);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));
        }

        EditText whitelist = (EditText) findViewById(R.id.deauth_modify);
        whitelist.setText(String.format(Locale.getDefault(),getString(R.string.loading_file), "/sdcard/nh_files/deauth/whitelist.txt"));
        exe.ReadFile_ASYNC("/sdcard/nh_files/deauth/whitelist.txt", whitelist);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        nh.showMessage("File Loaded");
    }


    public void updatewhitelist(View view) {
        EditText source = (EditText) findViewById(R.id.deauth_modify);
        String newSource = source.getText().toString();
        Boolean isSaved = exe.SaveFileContents(newSource, "/sdcard/nh_files/deauth/whitelist.txt");
        if (isSaved) {
            nh.showMessage("Source updated");
        } else {
            nh.showMessage("Source not updated");
        }
    }
}