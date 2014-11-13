package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class KaliServicesActivity extends Activity {

    // TOO MUCH COMENTS, SHOULD BE CLEANED

    // services array, easy to add/remove
    protected String[][] KaliServices =  new String[][]{

            // name, check  cmd, start cmd, stop cmd, state
            // since the script check-kaliweb isnt in the master i coment lighttp here
            //{"Lighttpd", "sh /system/xbin/check-kaliweb","start-web","stop-web"},
    		
            {"SSH", "sh /system/xbin/check-kalissh","start-ssh","stop-ssh"},
            {"Dnsmasq", "sh /system/xbin/check-kalidnsmq","start-dnsmasq","stop-dnsmasq"},
            {"Hostapd", "sh /system/xbin/check-kalihostapd","start-hostapd &","stop-hostapd"},
            {"VPN", "sh /system/xbin/check-kalivpn","start-vpn","stop-vpn"},
            {"Apache", "sh /system/xbin/check-kaliapache","start-apache","stop-apache"},
            {"Metasploit", "sh /system/xbin/check-kalimetasploit","start-msf","stop-msf"},
            // {"DHCP", "sh /system/xbin/check-kalidhcp","NOSCRIPT","NOSCRIPT"},
            // the stop script isnt working well, doing a raw cmd instead to stop vnc
            //{"VNC", "sh /system/xbin/check-kalivnc","bootkali\nvncserver","bootkali\nkill $(ps aux | grep 'Xtightvnc' | awk '{print $2}');CT=0;for x in $(ps aux | grep 'Xtightvnc' | awk '{print $2}'); do CT=$[$CT +1];tightvncserver -kill :$CT; done;rm /root/.vnc/*.log;rm -r /tmp/.X*"},
            // REMOVE THE INTENT FROM THE SCRIPT start-beef-xss!!! (sleep 35 \n am start -a android.intent.action.VIEW -d http://127.0.0.1:3000/ui/panel) not needed there.
            //{"BeefXSS", "sh /system/xbin/check-kalibeef-xss","start-beef-xss","stop-beef-xss"}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kali_services);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);

        checkServices();
    }



    private void checkServices() {
        //doit in the bg
        new Thread(new Runnable() {
            public void run() {

                ShellExecuter exe = new ShellExecuter();

                final ListView servicesList = (ListView) findViewById(R.id.servicesList);
                // generate check cmd with all the services
                String checkCmd = "";

                for (int i = 0; i < KaliServices.length; i++) {
                    checkCmd += KaliServices[i][1] + ";";
                }
                //Log.d("command", checkCmd);
                final String outp1 = exe.RunAsRootOutput(checkCmd);
                Log.d("output", outp1);
                // Once all its done, we have the states an can populate the listview
                servicesList.post(new Runnable() {
                    public void run() {
                        // New instance of the swichLoader
                        servicesList.setAdapter(new SwichLoader(getApplicationContext(), outp1, KaliServices));
                    }
                });

            }
        }).start();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

// This class is the main for the services


class SwichLoader extends BaseAdapter
{

    private Context mContext;
    private ShellExecuter sExec = new ShellExecuter();
    private String[] curstats;
    private String services[][];
    
    public SwichLoader(Context context, String serviceStates, String[][] KaliServices)
    {
        services = KaliServices;
        mContext=context;
        curstats =  serviceStates.split("(?!^)");
        Log.d("curstats", serviceStates);



    }
    static class ViewHolderItem {
        // The swich
        Switch sw;
        // the text holder
        TextView swholder;
    }
    public int getCount()
    {
        // return the number of services
        return  services.length;
    }

    // getView method is called for each item of ListView
    public View getView(final int position,  View convertView, ViewGroup parent)
    {
        // inflate the layout for each item of listView (our services)

        ViewHolderItem vH = null;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.swich_item, parent, false);

            // set up the ViewHolder
            vH = new ViewHolderItem();
            // get the reference of swicht and the text view
            vH.sw = (Switch) convertView.findViewById(R.id.switch1);
            vH.swholder = (TextView) convertView.findViewById(R.id.switchHolder);
            convertView.setTag(vH);
            //System.out.println ("created row");
        }
        else {
           // recicle the items in the list is allready exists
            vH = (ViewHolderItem) convertView.getTag();


        }
        // remove listeners
        vH.sw.setOnCheckedChangeListener(null);
        // set service name
        vH.sw.setText(services[position][0]);
        // clear state
        vH.sw.setChecked(false);
        // check it

        if(curstats[position].equals("1")){
            vH.sw.setChecked(true);
            vH.sw.setTextColor(mContext.getResources().getColor(R.color.blue));
            vH.swholder.setText(services[position][0] + " Service is currently UP");
            vH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));
        }
        else {
            vH.sw.setChecked(false);
            vH.sw.setTextColor(mContext.getResources().getColor(R.color.clearTitle));
            vH.swholder.setText(services[position][0] + " Service is currently DOWN");
            vH.swholder.setTextColor(mContext.getResources().getColor(R.color.clearText));
        }
	
        // add listeners
        final ViewHolderItem finalVH = vH;
        vH.sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    sExec.RunAsRoot(new String[]{services[position][2]});
                    curstats[position] = "1";
                    finalVH.swholder.setText(services[position][0] + " Service Started");
                    finalVH.sw.setTextColor(mContext.getResources().getColor(R.color.blue));
                    finalVH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));
                } else {

                    sExec.RunAsRoot(new String[]{services[position][3]});
                    curstats[position] = "0";
                    finalVH.swholder.setText(services[position][0] + " Service Stopped");
                    finalVH.sw.setTextColor(mContext.getResources().getColor(R.color.clearTitle));
                    finalVH.swholder.setTextColor(mContext.getResources().getColor(R.color.clearText));
                }
            }
        });

        return convertView;
    }


    public String[] getItem(int position) {

        return services[position];
    }



    public long getItemId(int position) {

        return position;
    }


}
