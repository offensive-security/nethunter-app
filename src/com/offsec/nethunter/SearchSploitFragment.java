package com.offsec.nethunter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;


import java.util.ArrayList;
import java.util.List;


public class SearchSploitFragment extends Fragment {

    private Context mContext;
    private static final String TAG = "SearchSploitFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ListView searchSploitListView;
    private ArrayList<String> exploitList;

    // Create and handle database
    private SearchSploitSQL database;

    public static SearchSploitFragment newInstance(int sectionNumber) {
        SearchSploitFragment fragment = new SearchSploitFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.searchsploit, container, false);
        mContext = getActivity().getApplicationContext();
        database = new SearchSploitSQL(mContext);

        // Search List



        // Search Bar
        final SearchView searchStr = (SearchView) rootView.findViewById(R.id.searchSploit_searchbar);

        final Button searchSearchSploit_git = (Button) rootView.findViewById(R.id.serchsploit_git);
        searchSearchSploit_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentClickListener_NH("cd /sdcard/nh_files && git clone https://github.com/offensive-security/exploit-database.git && echo \"DONE!\" && exit");
            }
        });
        // Load/reload database button
        final Button searchSearchSploit = (Button) rootView.findViewById(R.id.serchsploit_loadDB);
        searchSearchSploit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog pd = new ProgressDialog(getActivity());
                pd.setTitle("Feeding Exploit DB");
                pd.setMessage("This can take a minute, wait...");
                pd.setCancelable(false);
                pd.show();
                new Thread(new Runnable() {
                    public void run() {
                        final Boolean isFeeded = database.doDbFeed();

                        searchSearchSploit.post(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                if (isFeeded) {
                                    Toast.makeText(getActivity(),
                                            "DB FEED DONE",
                                            Toast.LENGTH_LONG).show();
                                    main(rootView);
                                } else {
                                    Toast.makeText(getActivity(),
                                            "Unable to find Searchsploit files.csv database. Install exploitdb in chroot",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        main(rootView);
                    }
                }, 250);
        return rootView;
    }

    private void main(final View rootView) {
        searchSploitListView = (ListView) rootView.findViewById(R.id.searchResultsList);
        List exploitList = database.getAllExploits();
        ExploitLoader exploitAdapter = new ExploitLoader(mContext, exploitList);
        searchSploitListView.setAdapter(exploitAdapter);
    }
    private void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();

        }
    }
}
class ExploitLoader extends BaseAdapter {

    private final List<SearchSploit> _exploitList;
    private Context _mContext;

    private ShellExecuter exe = new ShellExecuter();


    public ExploitLoader(Context context, List<SearchSploit> exploitList) {

        _mContext = context;
        _exploitList = exploitList;

    }

    static class ViewHolderItem {
        // The switch
        //Switch sw;
        // the msg holder

        TextView execmode;
        TextView sendtocmd;
        TextView runatboot;
        // the service title
        TextView cwTitle;
        // run at boot checkbox
        Button cwButton;
    }

    public int getCount() {
        // return the number of services
        return _exploitList.size();
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View convertView, ViewGroup parent) {
        // inflate the layout for each item of listView (our services)

        ViewHolderItem vH;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.searchsploit_item, parent, false);

            // set up the ViewHolder
            vH = new ViewHolderItem();
            // get the reference of switch and the text view
            vH.cwTitle = (TextView) convertView.findViewById(R.id.command_tag);
            // vH.cwSwich = (Switch) convertView.findViewById(R.id.switch1);
            vH.execmode = (TextView) convertView.findViewById(R.id.execmode);
            vH.sendtocmd = (TextView) convertView.findViewById(R.id.sendtocmd);
            vH.runatboot = (TextView) convertView.findViewById(R.id.custom_comands_runAtBoot_text);
            vH.cwButton = (Button) convertView.findViewById(R.id.runCommand);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list if already exists
            vH = (ViewHolderItem) convertView.getTag();
        }

        // remove listeners
        final SearchSploit exploitItem = getItem(position);

        final String _file = exploitItem.getFile();
        String _desc = exploitItem.getDescription();
        String _date = exploitItem.getDate();
        String _author = exploitItem.getAuthor();
        String _platform = exploitItem.getPlatform();
        String _type = exploitItem.getType();
        Integer _port = exploitItem.getPort();


        vH.cwButton.setOnClickListener(null);
        // set service name
        vH.cwTitle.setText(_desc);
        vH.execmode.setText(_platform);
        vH.sendtocmd.setText(_type);
        vH.runatboot.setText(_author);
        vH.cwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_mContext, EditSourceActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("path", "/sdcard/nh_files/exploit-database/" + _file);
                _mContext.startActivity(i);

            }
        });
        return convertView;

    }

    public SearchSploit getItem(int position) {
        return _exploitList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


}