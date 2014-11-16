package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class EditSourceActivity extends Activity {

    private String configFilePath = "";
    private Boolean shell = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        configFilePath = b.getString("path");
        if (getIntent().hasExtra("shell")) {
            shell = b.getBoolean("shell");
        }
        setContentView(R.layout.source);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));

        }
        EditText source = (EditText) findViewById(R.id.source);
        String text = "";
        if (shell) {
            text = readFileShell();
        } else {
            text = readFile();
        }
        source.setText(text);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }

    private String readFile() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, configFilePath);
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
        return text.toString();
    }

    private String readFileShell() {
        ShellExecuter exe = new ShellExecuter();
        return exe.Executer("cat " + configFilePath);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateSource(View arg0) {
        if (shell) {
            EditText source = (EditText) findViewById(R.id.source);
            String newSource = source.getText().toString();

            ShellExecuter exe = new ShellExecuter();
            String news = newSource;

            String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + news + "\nEOF"};
            exe.RunAsRoot(command);
            showMessage("Source updated");
        } else {
            try {
                File sdcard = Environment.getExternalStorageDirectory();
                File myFile = new File(sdcard, configFilePath);
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                EditText source = (EditText) findViewById(R.id.source);
                myOutWriter.append(source.getText());
                myOutWriter.close();
                fOut.close();
                showMessage("Source updated");
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(source.getWindowToken(), 0);
                super.onBackPressed();
            } catch (Exception e) {
                showMessage(e.getMessage());
            }
        }
    }

    private void showMessage(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}