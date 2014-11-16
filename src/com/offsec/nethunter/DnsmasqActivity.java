package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnsmasqActivity extends Activity {

    private String configFilePath = "files/dnsmasq.conf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dnsmasq);
        if (Build.VERSION.SDK_INT >= 21) {
            // detail for android 5 devices
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkTitle));

        }
        loadOptions();
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOptions();
    }

    private void loadOptions() {
        String text = readConfigFile();

	        /*
	         * Addresses
	         */
        EditText address1 = (EditText) findViewById(R.id.address1);
        EditText address2 = (EditText) findViewById(R.id.address2);
        String regExPatAddress = "^#{0,1}address=(.*)$";
        Pattern patternAddress = Pattern.compile(regExPatAddress, Pattern.MULTILINE);
        Matcher matcherAddress = patternAddress.matcher(text);

        ArrayList<String> addresses = new ArrayList<String>();
        while (matcherAddress.find()) {
            addresses.add(matcherAddress.group(1));
        }
        Integer a = 0;
        for (Iterator<String> i = addresses.iterator(); i.hasNext(); ) {
            String item = i.next();
            a++;
            if (a.equals(1)) {
                address1.setText(item);
            }
            if (a.equals(2)) {
                address2.setText(item);
            }
        }
	        /*
	         * Interface
	         */
        EditText ifc = (EditText) findViewById(R.id.ifc);
        String regExpatInterface = "^interface=(.*)$";
        Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
        Matcher matcherIfc = patternIfc.matcher(text);
        if (matcherIfc.find()) {
            String ifcValue = matcherIfc.group(1);
            ifc.setText(ifcValue);
        }
	        /*
	         * dhcp range
	         */
        EditText dhcpRange = (EditText) findViewById(R.id.dhcpRange);
        String regExpatDhcpRange = "^dhcp-range=(.*)$";
        Pattern patternDhcpRange = Pattern.compile(regExpatDhcpRange, Pattern.MULTILINE);
        Matcher matcherDhcpRange = patternDhcpRange.matcher(text);
        if (matcherDhcpRange.find()) {
            String dhcpRangeValue = matcherDhcpRange.group(1);
            dhcpRange.setText(dhcpRangeValue);
        }
	        /*
	         * dhcp options
	         */
        EditText dhcpOption1 = (EditText) findViewById(R.id.dhcpOption1);
        EditText dhcpOption2 = (EditText) findViewById(R.id.dhcpOption2);
        String regExPatDhcpOption = "dhcp-option=(.*)$";
        Pattern patternDhcpOption = Pattern.compile(regExPatDhcpOption, Pattern.MULTILINE);
        Matcher matcherDhcpOption = patternDhcpOption.matcher(text);

        ArrayList<String> dhcpOptions = new ArrayList<String>();
        while (matcherDhcpOption.find()) {
            dhcpOptions.add(matcherDhcpOption.group(1));
        }
        Integer b = 0;
        for (Iterator<String> i = dhcpOptions.iterator(); i.hasNext(); ) {
            String item = i.next();
            b++;
            if (b.equals(1)) {
                dhcpOption1.setText(item);
            }
            if (b.equals(2)) {
                dhcpOption2.setText(item);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dnsmasq, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.start_service:
                start();
                return true;
            case R.id.stop_service:
                stop();
                return true;
            case R.id.source_button:
                Intent i = new Intent(this, EditSourceActivity.class);
                i.putExtra("path", configFilePath);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    public void updateOptions(View arg0) {

        EditText address1 = (EditText) findViewById(R.id.address1);
        EditText address2 = (EditText) findViewById(R.id.address2);
        EditText ifc = (EditText) findViewById(R.id.ifc);
        EditText dhcpRange = (EditText) findViewById(R.id.dhcpRange);
        EditText dhcpOption1 = (EditText) findViewById(R.id.dhcpOption1);
        EditText dhcpOption2 = (EditText) findViewById(R.id.dhcpOption2);

        String source = readConfigFile();
        String regExPatAddress = "^#{0,1}address=(.*)$";
        Pattern patternAddress = Pattern.compile(regExPatAddress, Pattern.MULTILINE);
        Matcher matcherAddress = patternAddress.matcher(source);

        Integer a = 0;
        while (matcherAddress.find()) {
            a++;
            if (a.equals(1)) {
                if (matcherAddress.group(0).toString().startsWith("#")) {
                    source = source.replace(matcherAddress.group(0).toString(), "#address" + address1.getText().toString());
                } else {
                    source = source.replace(matcherAddress.group(0).toString(), "address" + address1.getText().toString());
                }
            }
            if (a.equals(2)) {
                if (matcherAddress.group(0).toString().startsWith("#")) {
                    source = source.replace(matcherAddress.group(0).toString(), "#address" + address2.getText().toString());
                } else {
                    source = source.replace(matcherAddress.group(0).toString(), "address" + address2.getText().toString());
                }
            }
        }
        source = source.replaceAll("(?m)interface=(.*)$", "interface=" + ifc.getText().toString());
        source = source.replaceAll("(?m)dhcp-range=(.*)$", "dhcp-range=" + dhcpRange.getText().toString());

        String regExPatDhcpOption = "dhcp-option=(.*)$";
        Pattern patternDhcpOption = Pattern.compile(regExPatDhcpOption, Pattern.MULTILINE);
        Matcher matcherDhcpOption = patternDhcpOption.matcher(source);

        a = 0;
        while (matcherDhcpOption.find()) {
            a++;
            if (a.equals(1)) {
                source = source.replace(matcherDhcpOption.group(0).toString(), "dhcp-option" + dhcpOption1.getText().toString());
            }
            if (a.equals(2)) {
                source = source.replace(matcherDhcpOption.group(0).toString(), "dhcp-option" + dhcpOption2.getText().toString());
            }
        }
        updateConfigFile(source);
        showMessage("Options updated");
    }


    public void start() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"start-dnsmasq"};
        exe.RunAsRoot(command);
        showMessage("Dnsmasq started");
    }

    public void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = {"stop-dnsmasq"};
        exe.RunAsRoot(command);
        showMessage("Dnsmasq stopped");
    }

    private void showMessage(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void updateConfigFile(String source) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard, configFilePath);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(source);
            myOutWriter.close();
            fOut.close();
            showMessage("Source updated");
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    private String readConfigFile() {
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
}
