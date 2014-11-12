package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

public class KaliServicesActivity extends Activity {
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kali_services);

        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
        
        Switch kaliSsh = (Switch) findViewById(R.id.kalissh);
        Switch kaliDnsmasq = (Switch) findViewById(R.id.kalidnsmasq);
        Switch kaliHostapd = (Switch) findViewById(R.id.kalihostapd);
        Switch kaliVpn = (Switch) findViewById(R.id.kalivpn);
        Switch kaliApache = (Switch) findViewById(R.id.kaliapache);
        Switch kaliMetasploit = (Switch) findViewById(R.id.kalimetasploit);
        
        ShellExecuter exe = new ShellExecuter();
        String outp1 = exe.RunAsRootOutput("sh /system/xbin/check-kalissh");
        
        boolean kaliSshStatus;
        if (outp1.equals("0")) {
        	kaliSshStatus = false;
        } else {
        	kaliSshStatus = true;
        }
        
        String outp2 = exe.RunAsRootOutput("sh /system/xbin/check-kalidnsmq");
        boolean kaliDnsmasqStatus;
        if (outp2.equals("0")) {
        	kaliDnsmasqStatus = false;
        } else {
        	kaliDnsmasqStatus = true;
        }
        
        String outp3 = exe.RunAsRootOutput("sh /system/xbin/check-kalihostapd");
        boolean kaliHostapdStatus;
        if (outp3.equals("0")) {
        	kaliHostapdStatus = false;
        } else {
        	kaliHostapdStatus = true;
        }
        
        String outp4 = exe.RunAsRootOutput("sh /system/xbin/check-kalivpn");
        boolean kaliVpnStatus;
        if (outp4.equals("0")) {
        	kaliVpnStatus = false;
        } else {
        	kaliVpnStatus = true;
        }
        
        String outp5 = exe.RunAsRootOutput("sh /system/xbin/check-kaliapache");
        boolean kaliApacheStatus;
        if (outp5.equals("0")) {
        	kaliApacheStatus = false;
        } else {
        	kaliApacheStatus = true;
        }
        
        String outp6 = exe.RunAsRootOutput("sh /system/xbin/check-kalimetasploit");
        boolean kaliMetasploitStatus;
        if (outp6.equals("0")) {
        	kaliMetasploitStatus = false;
        } else {
        	kaliMetasploitStatus = true;
        }
        		
        kaliSsh.setChecked(kaliSshStatus);
        kaliDnsmasq.setChecked(kaliDnsmasqStatus);
        kaliHostapd.setChecked(kaliHostapdStatus);
        kaliVpn.setChecked(kaliVpnStatus);
        kaliApache.setChecked(kaliApacheStatus);
        kaliMetasploit.setChecked(kaliMetasploitStatus);
        
        
        
        
        kaliSsh.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	
            	if (isChecked == true) {
            		String[] command = {"start-ssh"};
            		exe.RunAsRoot(command);
            		text = "Kali SSH started";
            	} else {
            		String[] command = {"stop-ssh"};
            		exe.RunAsRoot(command);
            		text = "Kali SSH stopped";
            	}
            	showToast(text);
            }
        });
        
        kaliDnsmasq.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	if (isChecked == true) {
            		String[] command = {"start-dnsmasq"};
            		exe.RunAsRoot(command);
            		text = "Kali dnsmasq started";
            	} else {
            		String[] command = {"stop-dnsmasq"};
            		exe.RunAsRoot(command);
            		text = "Kali dnsmasq stopped";
            	}
            	showToast(text);
            }
        });
        
        kaliHostapd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	if (isChecked == true) {
            		String[] command = {"start-hostapd &"};
            		exe.RunAsRoot(command); 
            		text = "Kali hostapd started";
            	} else {
            		String[] command = {"stop-hostapd"};
            		exe.RunAsRoot(command);
            		text = "Kali hostapd stopped";
            	}
            	showToast(text);
            }
        });
        
        kaliVpn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	if (isChecked == true) {
            		String[] command = {"start-vpn"};
            		exe.RunAsRoot(command); 
            		text = "Kali VPN started";
            	} else {
            		String[] command = {"start-vpn"};
            		exe.RunAsRoot(command);
            		text = "Kali VPN stopped";
            	}
            	showToast(text);
            }
        });
        
        kaliApache.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	if (isChecked == true) {
            		String[] command = {"start-apache"};
            		exe.RunAsRoot(command); 
            		text = "Kali apache started";
            	} else {
            		String[] command = {"stop-apache"};
            		exe.RunAsRoot(command);
            		text = "Kali apache stopped";
            	}
            	showToast(text);
            }
        });
        
        kaliMetasploit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	String text;
            	ShellExecuter exe = new ShellExecuter();
            	if (isChecked == true) {
            		String[] command = {"start-msf"};
            		exe.RunAsRoot(command); 
            		text = "Kali metasploit started";
            	} else {
            		String[] command = {"stop-msf"};
            		exe.RunAsRoot(command);
            		text = "Kali metasploit stopped";
            	}
            	showToast(text);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showToast(String message)
    {
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, message, duration);
    	toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
    	toast.show();
    }
}