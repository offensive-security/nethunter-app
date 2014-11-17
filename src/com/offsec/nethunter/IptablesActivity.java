package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class IptablesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iptables);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));

        }

        EditText source = (EditText) findViewById(R.id.source);
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "files/iptables.conf");
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
        }
        source.setText(text);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.run_iptables:
                runIptables();
                return true;
            case R.id.flush:
                flushIptables();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iptables, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void updateSource(View arg0) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard, "files/iptables.conf");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            EditText source = (EditText) findViewById(R.id.source);
            myOutWriter.append(source.getText());
            myOutWriter.close();
            fOut.close();
            showMessage("Source updated");
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    public void runIptables() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"start-iptables"};
        exe.RunAsRoot(command);
        showMessage("Iptables started");
    }

    public void flushIptables() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"iptables-flush"};
        exe.RunAsRoot(command);
        showMessage("Iptables flushed");
    }

    private void showMessage(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}