package com.offsec.nethunter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;



public class NethunterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nethunter);
        
        EditText interfaces = (EditText) findViewById(R.id.editText1);
        interfaces.setFocusable(false);
    	String intf = getInterfaces();   
        interfaces.setText(intf);
        
        EditText ip = (EditText) findViewById(R.id.editText2);
        ip.setFocusable(false);
        
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void getExternalIp(View arg0)
    {	
    	EditText ip = (EditText) findViewById(R.id.editText2);
    	ip.setText("Please wait...");
    	try {
    		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            
    		HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://myip.dnsomatic.com");
            HttpResponse response;
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
            	long len = entity.getContentLength();
            	if (len != -1 && len < 1024) {
            		String str=EntityUtils.toString(entity);
            		ip.setText(str);
            	} else {
            		ip.setText("Response too long or error.");
            	}            
            } else {
            	ip.setText("Null:"+response.getStatusLine().toString());
            }
    	}
    	catch (Exception e) {
    		//Log.e("Nethunter", "exception", e);
    		ip.setText("Error");
    	}
    }
    
    private String getInterfaces()
    {
    	String command[] = {"sh", "-c", "netcfg |grep UP |grep -v ^lo|awk -F\" \" '{print $1\"\t\" $3}'"};
    	ShellExecuter exe = new ShellExecuter();
        String outp = exe.Executer(command);
        return outp;
        
    }
}