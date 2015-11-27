package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.database.sqlite.SQLiteDatabase;
import static android.database.DatabaseUtils.sqlEscapeString;


public class SearchSploitFragment extends Fragment {

    SharedPreferences sharedpreferences;
    private Context mContext;
    static NhPaths nh;
    private static final String fileName = "/data/local/nhsystem/kali-armhf/usr/share/exploitdb/files.csv";
    private static final String databasefileName = "/data/data/com.offsec.nethunter/databases/";
    private static final String TAG = "SearchSploitFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ListView searchSploitListView;
    private ArrayList<String> exploitList;

    // Create and handle database
    private SearchSploitSQLiteHelper database;

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
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();
        database = new SearchSploitSQLiteHelper(mContext);

        // Search List
        searchSploitListView = (ListView) rootView.findViewById(R.id.searchResultsList);


        // Search Bar
        final SearchView searchStr = (SearchView) rootView.findViewById(R.id.searchSploit_searchbar);




        // Load/reload database button
        final Button searchSearchSploit = (Button) rootView.findViewById(R.id.serchsploit_loadDB);
        searchSearchSploit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File chrootfile = new File(fileName);
                File databasefile = new File(databasefileName);

                SearchSploitSQLiteHelper mDbHelper = new SearchSploitSQLiteHelper(getActivity());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                if (chrootfile.exists()) {
                    Log.d(TAG, "files.csv found in chroot " + fileName);
                    if (databasefile.exists()) {
                        // If database exists, remove old table and create new one
                        db.execSQL("DROP TABLE IF EXISTS ssploitdb");
                        db.execSQL("CREATE TABLE ssploitdb ( _id INTEGER PRIMARY KEY, " +
                                "file TEXT," +
                                "description TEXT," +
                                "date DATE," +
                                "author TEXT," +
                                "platform TEXT," +
                                "type TEXT," +
                                "port INTEGER)");
                    }

                    FileReader file = null;
                    try {
                        file = new FileReader(fileName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader buffer = new BufferedReader(file);
                    try {
                        buffer.readLine(); // Reads the first line (header) and throws away
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String line = "";
                    String tableName = "ssploitdb";
                    String columns = "_id, file, description, date, author, platform, type, port";
                    String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
                    String str2 = ");";

                    db.beginTransaction();
                    try {
                        while ((line = buffer.readLine()) != null) {
                            StringBuilder sb = new StringBuilder(str1);
                            String[] str = line.split(",");
                            sb.append("'" + str[0] + "',");
                            sb.append("'" + str[1] + "',");

                            //We need to escape single quotes in description
                            String description = sqlEscapeString(str[2]);

                            sb.append("" + description + ",");
                            sb.append("'" + str[3] + "',");

                            String author = sqlEscapeString(str[4]);

                            sb.append("" + author + ",");
                            sb.append("'" + str[5] + "',");
                            sb.append("'" + str[6] + "',");
                            sb.append("'" + str[7] + "'");
                            sb.append(str2);
                            db.execSQL(sb.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                } else {
                    Toast.makeText(getActivity(), "Unable to find Searchsploit files.csv database." +
                                    " Install exploitdb in chroot",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }
}