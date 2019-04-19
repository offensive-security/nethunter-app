package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;

import androidx.fragment.app.Fragment;

public class KaliServicesFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private String[][] KaliServices; //
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String RUN_AT_BOOT = "RUN_AT_BOOT";
    private NhPaths nh;
    private SharedPreferences prefs;

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
        setHasOptionsMenu(true);
        checkServices(rootView);

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        prefs = getContext().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        inflater.inflate(R.menu.kali_services, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.bootServicesState);
        if (item != null) {
            if (prefs.getBoolean(RUN_AT_BOOT, true)) {
                item.setTitle("DISABLE Services At Boot");
            } else {
                item.setTitle("ENABLE Services At Boot");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.bootServicesState:
                if (prefs.getBoolean(RUN_AT_BOOT, true)) {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean(RUN_AT_BOOT, false);
                    ed.apply();
                    nh.showMessage("Boot Services DISABLED");
                } else {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean(RUN_AT_BOOT, true);
                    ed.apply();
                    nh.showMessage("Boot Services ENABLED");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        nh = new NhPaths();

        KaliServices = new String[][]{

                // {name, check_cmd, start_cmd, stop_cmd, init_service_filename}

                {"SSH", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalissh", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali ssh start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali ssh stop'", "70ssh"},
                {"Dnsmasq", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalidnsmq", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali dnsmasq start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali dnsmasq stop'", "70dnsmasq"},
                {"Hostapd", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalihostapd", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hostapd start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali hostapd stop'", "70hostapd"},
                {"OpenVPN", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalivpn", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali openvpn start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali openvpn stop'", "70openvpn"},
                {"Apache", "sh " + nh.APP_SCRIPTS_PATH + "/check-kaliapache", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali apache start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali apache stop'", "70apache"},
                {"Metasploit", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalimetasploit", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali msf start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali msf stop'", "70msf"},
                {"Fruity WiFi", "sh " + nh.APP_SCRIPTS_PATH + "/check-fruity-wifi", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali fruitywifi start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali fruitywifi stop'", "70fruity"},
                //{"DHCP", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalidhcp","su -c '" + cachedir + "/bootkali dhcp start'","su -c '" + cachedir + "/bootkali dhcp stop'", "70dhcp"},
                {"BeEF Framework", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalibeef-xss", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali beef-xss start'", "su -c '" + nh.APP_SCRIPTS_PATH + "/bootkali beef-xss stop'", "70beef"},
                {"Y-cable Charging", "sh " + nh.APP_SCRIPTS_PATH + "/check-ycable", "su -c 'bootkali ycable start'", "su -c 'bootkali ycable stop'", "70ycable"}
                // the stop script isnt working well, doing a raw cmd instead to stop vnc
                // {"VNC", "sh " + nh.APP_SCRIPTS_PATH + "/check-kalivnc", "" + cachedir + "/bootkali\nvncserver", "" + cachedir + "/bootkali\nkill $(ps aux | grep 'Xtightvnc' | awk '{print $2}');CT=0;for x in $(ps aux | grep 'Xtightvnc' | awk '{print $2}'); do CT=$[$CT +1];tightvncserver -kill :$CT; done;rm /root/.vnc/*.log;rm -r /tmp/.X*", "70vnc"},
        };
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }


    private void checkServices(final View rootView) {

        new Thread(() -> {

            nh = new NhPaths();

            ShellExecuter exe = new ShellExecuter();
            final ListView servicesList = rootView.findViewById(R.id.servicesList);
            String checkCmd = "";
            String checkBootStates = "";
            final String bootScriptPath = nh.APP_INITD_PATH;

            if (KaliServices == null) {
                Log.d("Services", "Null KaliServices");
            } else {

                for (String[] KaliService : KaliServices) {
                    Log.d("bootScriptPath", KaliService[4]);
                }

                for (String[] KaliService : KaliServices) {
                    Log.d("bootScriptPath", bootScriptPath + "/" + KaliService[4]);

                    File checkBootFile = new File(bootScriptPath + "/" + KaliService[4]);
                    if (checkBootFile.exists()) {
                        checkBootStates += "1";
                    } else {
                        checkBootStates += "0";
                    }
                    checkCmd += KaliService[1] + ";";
                }

                final String serviceStates = exe.RunAsRootOutput(checkCmd);
                final String finalCheckBootStates = checkBootStates;
                servicesList.post(() -> servicesList.setAdapter(new KaliServicesLoader(getActivity().getApplicationContext(), serviceStates, finalCheckBootStates, KaliServices, bootScriptPath)));

            }
        }).start();
    }

}


// This class is the main for the services


class KaliServicesLoader extends BaseAdapter {

    private final Context mContext;
    private final String[] _serviceStates;
    private final String[] _serviceBootStates;
    private final String[][] services;
    private final String bootScriptPath;
    private final String shebang;
    private final ShellExecuter exe = new ShellExecuter();


    KaliServicesLoader(Context context, String serviceStates, String bootStates, String[][] KaliServices, String _bootScriptPath) {

        mContext = context;

        services = KaliServices;
        _serviceStates = serviceStates.split("(?!^)");
        _serviceBootStates = bootStates.split("(?!^)");

        bootScriptPath = _bootScriptPath;
        shebang = "#!/system/bin/sh\n\n# Init at boot kaliSevice: ";

    }

    private static class ViewHolderItem {
        // The switch
        Switch sw;
        // the msg holder
        TextView swholder;
        // the service title
        TextView swTitle;
        // run at boot checkbox
        CheckBox swBootCheckbox;
    }

    public int getCount() {
        // return the number of services
        return services.length;
    }

    private void addBootService(int serviceId) {
        String bootServiceFile = bootScriptPath + "/" + services[serviceId][4];
        String fileContents = shebang + services[serviceId][0] + "\n" + services[serviceId][2];
        Log.d("bootScript", fileContents);
        exe.RunAsRoot(new String[]{
                "cat > " + bootServiceFile + " <<s0133717hur75\n" + fileContents + "\ns0133717hur75\n",
                "chmod 700 " + bootServiceFile
        });

        // return the number of services

    }

    private void removeBootService(int serviceId) {
        // return the number of services
        String bootServiceFile = bootScriptPath + "/" + services[serviceId][4];
        exe.RunAsRoot(new String[]{"rm -rf " + bootServiceFile});
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View convertView, ViewGroup parent) {
        // inflate the layout for each item of listView (our services)

        ViewHolderItem vH;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.kali_services_item, parent, false);

            // set up the ViewHolder
            vH = new ViewHolderItem();
            // get the reference of switch and the text view
            vH.swTitle = convertView.findViewById(R.id.switchTitle);
            vH.sw = convertView.findViewById(R.id.switch1);
            vH.swholder = convertView.findViewById(R.id.switchHolder);
            vH.swBootCheckbox = convertView.findViewById(R.id.initAtBoot);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list is already exists
            vH = (ViewHolderItem) convertView.getTag();
        }
        if (position >= _serviceStates.length) {
            // out of range, return ,do nothing
            return convertView;
        }
        // remove listeners
        vH.sw.setOnCheckedChangeListener(null);
        vH.swBootCheckbox.setOnCheckedChangeListener(null);
        // set service name
        vH.swTitle.setText(services[position][0]);
        // clear state
        vH.sw.setChecked(false);
        vH.swBootCheckbox.setChecked(false);
        // check it

        // running services
        if (_serviceStates[position].equals("1")) {
            vH.sw.setChecked(true);
            vH.swTitle.setTextColor(mContext.getResources().getColor(R.color.blue));
            vH.swholder.setText(services[position][0] + " Service is UP");
            vH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
            vH.sw.setChecked(false);

            vH.swTitle.setTextColor(mContext.getResources().getColor(R.color.clearTitle));
            vH.swholder.setText(services[position][0] + " Service is DOWN");
            vH.swholder.setTextColor(mContext.getResources().getColor(R.color.clearText));
        }
        // services enabled at boot
        if (_serviceBootStates[position].equals("1")) {
            // is enabled
            vH.swBootCheckbox.setChecked(true);
            vH.swBootCheckbox.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
            // is not :)
            vH.swBootCheckbox.setChecked(false);
            vH.swBootCheckbox.setTextColor(mContext.getResources().getColor(R.color.clearTitle));
        }

        // add listeners
        final ViewHolderItem finalVH = vH;
        vH.sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new Thread(() -> exe.RunAsRoot(new String[]{services[position][2]})).start();
                _serviceStates[position] = "1";
                finalVH.swholder.setText(services[position][0] + " Service Started");
                finalVH.swTitle.setTextColor(mContext.getResources().getColor(R.color.blue));
                finalVH.swholder.setTextColor(mContext.getResources().getColor(R.color.blue));

            } else {
                new Thread(() -> exe.RunAsRoot(new String[]{services[position][3]})).start();
                _serviceStates[position] = "0";
                finalVH.swholder.setText(services[position][0] + " Service Stopped");
                finalVH.swTitle.setTextColor(mContext.getResources().getColor(R.color.clearTitle));
                finalVH.swholder.setTextColor(mContext.getResources().getColor(R.color.clearText));

            }
        });
        vH.swBootCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new Thread(() -> {
                    Log.d("bootservice", "ADD " + services[position][4]);
                    addBootService(position);
                }).start();
                _serviceBootStates[position] = "1";
                finalVH.swBootCheckbox.setTextColor(mContext.getResources().getColor(R.color.blue));
            } else {
                new Thread(() -> {
                    Log.d("bootservice", "REMOVE " + services[position][4]);
                    removeBootService(position);
                }).start();
                _serviceBootStates[position] = "0";
                finalVH.swBootCheckbox.setTextColor(mContext.getResources().getColor(R.color.clearTitle));

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
