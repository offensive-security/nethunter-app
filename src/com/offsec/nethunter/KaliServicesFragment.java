package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class KaliServicesFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private String[][] KaliServices;
    private static final String ARG_SECTION_NUMBER = "section_number";
    boolean updateStatuses = false;

    public KaliServicesFragment() {

    }


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static KaliServicesFragment newInstance(int sectionNumber) {
        KaliServicesFragment fragment = new KaliServicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.kali_services, container, false);
        checkServices(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (isAdded()) {
            String fileDir = getActivity().getFilesDir().toString() + "/scripts";

            KaliServices = new String[][]{

                    // name, check  cmd, start cmd, stop cmd, state

                    {"SSH", "sh " + fileDir + "/check-kalissh", "su -c '" + fileDir + "/bootkali ssh start'", "su -c '" + fileDir + "/bootkali ssh stop'"},
                    {"Dnsmasq", "sh " + fileDir + "/check-kalidnsmq", "su -c '" + fileDir + "/bootkali dnsmasq start'", "su -c '" + fileDir + "/bootkali dnsmasq stop'"},
                    {"Hostapd", "sh " + fileDir + "/check-kalihostapd", "su -c '" + fileDir + "/bootkali hostapd start'", "su -c '" + fileDir + "/bootkali hostapd stop'"},
                    {"OpenVPN", "sh " + fileDir + "/check-kalivpn", "su -c '" + fileDir + "/bootkali openvpn start'", "su -c '" + fileDir + "/bootkali openvpn stop'"},
                    {"Apache", "sh " + fileDir + "/check-kaliapache", "su -c '" + fileDir + "/bootkali apache start'", "su -c '" + fileDir + "/bootkali apache stop'"},
                    {"Metasploit", "sh " + fileDir + "/check-kalimetasploit", "su -c '" + fileDir + "/bootkali msf start'", "su -c '" + fileDir + "/bootkali msf stop'"},
                    //{"DHCP", "sh " + fileDir + "/check-kalidhcp","su -c '" + cachedir + "/bootkali dhcp start'","su -c '" + cachedir + "/bootkali dhcp stop'"},
                    {"BeEF Framework", "sh " + fileDir + "/check-kalibeef-xss", "su -c '" + fileDir + "/bootkali beef-xss start'", "su -c '" + fileDir + "/bootkali beef-xss stop'"},
                    //{"Fruity WiFi", "sh " + fileDir + "/check-fruity-wifi","su -c start-fruity-wifi","su -c  stop-fruity-wifi"}
                    // the stop script isnt working well, doing a raw cmd instead to stop vnc
                    // {"VNC", "sh " + fileDir + "/check-kalivnc", "" + cachedir + "/bootkali\nvncserver", "" + cachedir + "/bootkali\nkill $(ps aux | grep 'Xtightvnc' | awk '{print $2}');CT=0;for x in $(ps aux | grep 'Xtightvnc' | awk '{print $2}'); do CT=$[$CT +1];tightvncserver -kill :$CT; done;rm /root/.vnc/*.log;rm -r /tmp/.X*"},
            };
        }
    }

    public void onResume()
    {
        super.onResume();
        updateStatuses = true;
    }

    public void onPause()
    {
        super.onPause();
        updateStatuses = false;
    }

    public void onStop()
    {
        super.onStop();
        updateStatuses = false;
    }


    private void checkServices(final View rootView) {

        new Thread(new Runnable() {

            public void run() {

                ShellExecuter exe = new ShellExecuter();
                final ListView servicesList = (ListView) rootView.findViewById(R.id.servicesList);
                String checkCmd = "";
                for (String[] KaliService : KaliServices) {
                    checkCmd += KaliService[1] + ";";
                }
                final String outp1 = exe.RunAsRootOutput(checkCmd);
                servicesList.post(new Runnable() {
                    @Override
                    public void run() {
                        servicesList.setAdapter(new SwichLoader(getActivity().getApplicationContext(), outp1, KaliServices));
                    }
                });

            }

        }).start();
    }
}


// This class is the main for the services


class SwichLoader extends BaseAdapter {

    private Context mContext;
    private String[] curstats;
    private String services[][];
    private ShellExecuter exe = new ShellExecuter();
    public SwichLoader(Context context, String serviceStates, String[][] KaliServices) {
        services = KaliServices;
        mContext = context;
        curstats = serviceStates.split("(?!^)");

    }

    static class ViewHolderItem {
        // The swich
        Switch sw;
        // the text holder
        TextView swholder;
    }

    public int getCount() {
        // return the number of services
        return services.length;
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View convertView, ViewGroup parent) {
        // inflate the layout for each item of listView (our services)

        ViewHolderItem vH;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.swich_item, parent, false);

            // set up the ViewHolder
            vH = new ViewHolderItem();
            // get the reference of switch and the text view
            vH.sw = (Switch) convertView.findViewById(R.id.switch1);
            vH.swholder = (TextView) convertView.findViewById(R.id.switchHolder);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list is already exists
            vH = (ViewHolderItem) convertView.getTag();


        }
        // remove listeners
        vH.sw.setOnCheckedChangeListener(null);
        // set service name
        vH.sw.setText(services[position][0]);
        // clear state
        vH.sw.setChecked(false);
        // check it

        if (position>=curstats.length) {
            return convertView;
        }
        if (curstats[position].equals("1")) {
            vH.sw.setChecked(true);
            vH.sw.setTextColor(mContext.getResources().getColor(R.color.blue));
            vH.swholder.setText(services[position][0] + " Service is currently UP");
            vH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
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
                    new Thread(new Runnable() {
                        public void run() {
                            exe.RunAsRoot(new String[]{services[position][2]});
                        }

                    }).start();
                    curstats[position] = "1";
                    finalVH.swholder.setText(services[position][0] + " Service Started");
                    finalVH.sw.setTextColor(mContext.getResources().getColor(R.color.blue));
                    finalVH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));

                } else {
                    new Thread(new Runnable() {
                        public void run() {
                            exe.RunAsRoot(new String[]{services[position][3]});
                        }

                    }).start();
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