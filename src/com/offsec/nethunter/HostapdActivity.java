package com.offsec.nethunter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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


public class HostapdActivity extends Activity {
    
    private String configFilePath = "files/hostapd.conf";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostapd);
        loadOptions();
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }
    
    @Override
	public void onResume() {
		super.onResume();
		loadOptions();
	}
    
    private void loadOptions()
    {
    	String text = readConfigFile();
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
         * bssid
         */
        EditText bssid = (EditText) findViewById(R.id.bssid);
        String regExpatbssid = "^bssid=(.*)$";
        Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
        Matcher matcherBssid = patternBssid.matcher(text);
        if (matcherBssid.find()) {
        	String bssidVal = matcherBssid.group(1);
        	bssid.setText(bssidVal);
        }
        /*
         * ssid
         */
        EditText ssid = (EditText) findViewById(R.id.ssid);
        String regExpatssid = "^ssid=(.*)$";
        Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
        Matcher matcherSsid = patternSsid.matcher(text);
        if (matcherSsid.find()) {
        	String ssidVal = matcherSsid.group(1);
        	ssid.setText(ssidVal);
        }
        /*
         * channel
         */
        EditText channel = (EditText) findViewById(R.id.channel);
        String regExpatChannel = "^channel=(.*)$";
        Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
        Matcher matcherChannel = patternChannel.matcher(text);
        if (matcherChannel.find()) {
        	String channelVal = matcherChannel.group(1);
        	channel.setText(channelVal);
        }
        /*
         * enable_karma
         */
        EditText enableKarma = (EditText) findViewById(R.id.enableKarma);
        String regExpatEnableKarma = "^enable_karma=(.*)$";
        Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
        Matcher matcherEnableKarma = patternEnableKarma.matcher(text);
        if (matcherEnableKarma.find()) {
        	String enableKarmaVal = matcherEnableKarma.group(1);
        	enableKarma.setText(enableKarmaVal);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
            	NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.start_service:
            	startHostapd();
                return true;
            case R.id.stop_service:
            	stopHostapd();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hostapd, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    public void updateOptions(View arg0)
    {
    	String source = readConfigFile();
    	EditText ifc = (EditText) findViewById(R.id.ifc);
    	EditText bssid = (EditText) findViewById(R.id.bssid);
    	EditText ssid = (EditText) findViewById(R.id.ssid);
    	EditText channel = (EditText) findViewById(R.id.channel);
    	EditText enableKarma = (EditText) findViewById(R.id.enableKarma);
    	
    	source = source.replaceAll("(?m)^interface=(.*)$", "interface="+ifc.getText().toString());
    	source = source.replaceAll("(?m)^bssid=(.*)$", "bssid="+bssid.getText().toString());
    	source = source.replaceAll("(?m)^ssid=(.*)$", "ssid="+ssid.getText().toString());
    	source = source.replaceAll("(?m)^channel=(.*)$", "channel="+channel.getText().toString());
    	source = source.replaceAll("(?m)^enable_karma=(.*)$", "enable_karma="+enableKarma.getText().toString());
    	
    	updateConfigFile(source);
    	showMessage("Options updated");
    }
    
    public void updateSource(View arg0)
    {
    	EditText source = (EditText) findViewById(R.id.source);
    	updateConfigFile(source.getText().toString());
    }
    
    public void startHostapd()
    {
    	ShellExecuter exe = new ShellExecuter();
    	String[] command = {"start-hostapd &"};
		exe.RunAsRoot(command);
    	showMessage("Hostapd started");
    }
    
    public void stopHostapd()
    {
    	ShellExecuter exe = new ShellExecuter();
    	String[] command = {"stop-hostapd"};
		exe.RunAsRoot(command);
    	showMessage("Hostapd stopped");
    }
    
    private void showMessage(String message)
    {
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, message, duration);
    	toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
    	toast.show();
    }
    
    private void updateConfigFile(String source)
    {
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
    
    private String readConfigFile()
    {
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
        }
        catch (IOException e) {
        	Log.e("Nethunter", "exception", e);
        }
    	return text.toString();
    }
}