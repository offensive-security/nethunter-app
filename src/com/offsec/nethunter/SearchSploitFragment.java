package com.offsec.nethunter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

//import androidx.appcompat.widget.SearchView;


public class SearchSploitFragment extends Fragment {

    private Context mContext;
    private static final String TAG = "SearchSploitFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";


    private Boolean withFilters = true;
    private String sel_type;
    private String sel_platform;
    private String sel_search = "";
    private TextView numex;
    private AlertDialog adi;
    private Boolean isLoaded = false;
    private ListView searchSploitListView;
    private List<SearchSploit> full_exploitList;
    // Create and handle database
    private SearchSploitSQL database;
    private NhPaths nh;

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

        nh = new NhPaths();
        setHasOptionsMenu(true);
        database = new SearchSploitSQL(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Exploit Database Archive");
        builder.setMessage("Loading...wait");

        adi = builder.create();
        adi.setCancelable(false);
        adi.show();
        // Search Bar
        numex = rootView.findViewById(R.id.numex);
        final SearchView searchStr = rootView.findViewById(R.id.searchSploit_searchbar);
        searchStr.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 1) {
                    sel_search = query;
                } else {
                    sel_search = "";
                }
                loadExploits();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() == 0) {
                    sel_search = "";
                    loadExploits();
                }

                return false;
            }
        });
        // Load/reload database button
        final Button searchSearchSploit = rootView.findViewById(R.id.serchsploit_loadDB);
        searchSearchSploit.setVisibility(View.GONE);
        searchSearchSploit.setOnClickListener(v -> {
            final ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setTitle("Feeding Exploit DB");
            pd.setMessage("This can take a minute, wait...");
            pd.setCancelable(false);
            pd.show();
            new Thread(() -> {
                final Boolean isFeeded = database.doDbFeed();
                searchSearchSploit.post(() -> {
                    if (isFeeded) {
                        Toast.makeText(getActivity(),
                                "DB FEED DONE",
                                Toast.LENGTH_LONG).show();
                        try {
                            // Search List
                            //String sd = nh.SD_PATH;
                            String sd = "/sdcard";
                            //String data = nh.APP_PATH;
                            String data = "/data/data/com.offsec.nethunter/files/";
                            String DATABASE_NAME = "SearchSploit";
                            String currentDBPath = "../databases/" + DATABASE_NAME;
                            String backupDBPath = "/nh_files/" + DATABASE_NAME; // From SD directory.

                            File backupDB = new File(data, currentDBPath);
                            File currentDB = new File(sd, backupDBPath);

                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());

                            src.close();
                            dst.close();
                            Log.d("importDB", "Successfuly imported " + DATABASE_NAME);
                            main(rootView);
                            pd.dismiss();
                        } catch (Exception e) {
                            Log.d("importDB", e.toString());
                        }
                        // main(rootView);
                    } else {
                        Toast.makeText(getActivity(),
                                "Unable to find Searchsploit files.csv database. Install exploitdb in chroot",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });
        //prevents menu stuck
        new android.os.Handler().postDelayed(
                () -> main(rootView), 250);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searchsploit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rawSearch_ON:
                if (getView() == null) return true;
                if (!withFilters) {
                    getView().findViewById(R.id.search_filters).setVisibility(View.VISIBLE);
                    withFilters = true;
                    item.setTitle("Enable Raw search");
                    loadExploits();
                    hideSoftKeyboard(getView());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Raw search warning");

                    builder.setMessage("The exploit db is pretty big (+30K exploits), activating raw search will make the search slow.\nIs useful to do global searches when you don't find a exploit.")
                            .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                            .setPositiveButton("Enable", (dialog, id) -> {
                                getView().findViewById(R.id.search_filters).setVisibility(View.GONE);
                                item.setTitle("Disable Raw search");
                                withFilters = false;
                                loadExploits();
                                hideSoftKeyboard(getView());
                            });

                    AlertDialog ad = builder.create();
                    ad.setCancelable(false);
                    ad.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void hideSoftKeyboard(final View caller) {
        caller.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) caller.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(caller.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }, 100);
    }

    private void main(final View rootView) {

        searchSploitListView = rootView.findViewById(R.id.searchResultsList);
        Long exploitCount = database.getCount();
        Button searchSearchSploit = rootView.findViewById(R.id.serchsploit_loadDB);
        if (exploitCount == 0) {
            searchSearchSploit.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.search_filters).setVisibility(View.GONE);
            adi.dismiss();
            hideSoftKeyboard(getView());
            return;
        } else {
            rootView.findViewById(R.id.search_filters).setVisibility(View.VISIBLE);
            searchSearchSploit.setVisibility(View.GONE);
        }

        final List<String> platformList = database.getPlatforms();
        Spinner platformSpin = rootView.findViewById(R.id.exdb_platform_spinner);
        ArrayAdapter<String> adp12 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, platformList);
        adp12.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformSpin.setAdapter(adp12);
        platformSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sel_platform = platformList.get(position);
                loadExploits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        final List<String> typeList = database.getTypes();
        Spinner typeSpin = rootView.findViewById(R.id.exdb_type_spinner);
        ArrayAdapter<String> adp13 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, typeList);
        adp13.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpin.setAdapter(adp13);
        typeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sel_type = typeList.get(position);
                loadExploits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        loadExploits();
    }

    private void loadExploits() {
        if ((sel_platform != null) && (sel_type != null)) {
            List<SearchSploit> exploitList;
            if (withFilters) {
                exploitList = database.getAllExploitsFiltered(sel_search, sel_type, sel_platform);
            } else {
                if (sel_search.equals("")) {
                    exploitList = full_exploitList;
                } else {
                    exploitList = database.getAllExploitsRaw(sel_search);
                }
            }
            if (exploitList == null) {
                new android.os.Handler().postDelayed(
                        () -> loadExploits(), 1500);
                return;
            }
            numex.setText(String.format("%d results", exploitList.size()));
            ExploitLoader exploitAdapter = new ExploitLoader(mContext, exploitList);
            searchSploitListView.setAdapter(exploitAdapter);
            if (!isLoaded) {
                // preloading the long list lets see if is more performant
                // preload in the background.
                new Thread(() -> full_exploitList = database.getAllExploitsRaw("")).start();

                adi.dismiss();
                isLoaded = true;
                hideSoftKeyboard(getView());
            }
        }
    }
}

class ExploitLoader extends BaseAdapter {

    private final List<SearchSploit> _exploitList;
    private final Context _mContext;
    static NhPaths nh;


    ExploitLoader(Context context, List<SearchSploit> exploitList) {

        _mContext = context;
        _exploitList = exploitList;

    }

    static class ViewHolderItem {
        // The switch
        //Switch sw;
        // the msg holder
        TextView type;
        TextView platform;
        TextView author;
        TextView date;
        // the service title
        TextView description;
        // run at boot checkbox
        Button viewSource;
        Button openWeb;
        Button sendHid;
    }

    public int getCount() {
        // return the number of services
        return _exploitList.size();
    }

    private void start(String file) {
        String[] command = new String[1];
        command[0] = "su -c /data/data/com.offsec.nethunter/files/scripts/bootkali file2hid-file " + file;
        String test = "su -c /data/data/com.offsec.nethunter/files/scripts/bootkali file2hid-file " + file;
        Log.d("Exe:", test);
        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRoot(command);
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
            vH.description = convertView.findViewById(R.id.description);
            // vH.cwSwich = (Switch) convertView.findViewById(R.id.switch1);
            vH.type = convertView.findViewById(R.id.type);
	    vH.platform = convertView.findViewById(R.id.platform);
            vH.author = convertView.findViewById(R.id.author);
            vH.date = convertView.findViewById(R.id.exploit_date);
            vH.viewSource = convertView.findViewById(R.id.viewSource);
            vH.openWeb = convertView.findViewById(R.id.openWeb);
            vH.sendHid = convertView.findViewById(R.id.searchsploit_sendhid_button);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list if already exists
            vH = (ViewHolderItem) convertView.getTag();
        }

        // remove listeners
        final SearchSploit exploitItem = getItem(position);

        final String _file = exploitItem.getFile();
        final Long _id = exploitItem.getId();
        String _desc = exploitItem.getDescription();
        String _date = exploitItem.getDate();
        String _author = exploitItem.getAuthor();
        String _type = exploitItem.getType();
        String _platform = exploitItem.getPlatform();

        vH.viewSource.setOnClickListener(null);
        vH.openWeb.setOnClickListener(null);
        // set service name
        vH.description.setText(_desc);
        vH.type.setText(_type);
	vH.platform.setText(_platform);
        vH.author.setText(_author);
        vH.date.setText(_date);
        vH.viewSource.setOnClickListener(v -> {
            Intent i = new Intent(_mContext, EditSourceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("path", "/data/local/nhsystem/kali-armhf/usr/share/exploitdb/" + _file);
            _mContext.startActivity(i);

        });
        vH.sendHid.setOnClickListener(v -> {
            start("/usr/share/exploitdb/" + _file);
            //_mContext.startActivity(i);

        });
        vH.openWeb.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String url = "https://www.exploit-db.com/exploits/" + _id + "/";
            i.setData(Uri.parse(url));
            _mContext.startActivity(i);
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
