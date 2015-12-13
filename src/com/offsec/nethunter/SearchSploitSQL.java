package com.offsec.nethunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.offsec.nethunter.utils.NhPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static android.database.DatabaseUtils.sqlEscapeString;

class SearchSploitSQL extends SQLiteOpenHelper {
    NhPaths nh;
    Context context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SearchSploit";
    private static final String TAG = "SearchSploitSQL";
    private static final String CSVfileName = "/sdcard/nh_files/exploit-database/files.csv";
    private static final String databasefileName = "/data/data/com.offsec.nethunter/databases/";
    public SearchSploitSQL(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        nh = new NhPaths();
    }

    private String CREATE_SEARCHSPLOIT_TABLE =
            "CREATE TABLE " + SearchSploit.TABLE +
                    " (" + SearchSploit.ID + " INTEGER PRIMARY KEY, " +
                    SearchSploit.FILE + " TEXT," +
                    SearchSploit.DESCRIPTION + " TEXT," +
                    SearchSploit.DATE + " TEXT," +
                    SearchSploit.AUTHOR + " TEXT," +
                    SearchSploit.PLATFORM + " TEXT," +
                    SearchSploit.TYPE + " TEXT," +
                    SearchSploit.PORT + " INTEGER)";

    public void onCreate(SQLiteDatabase database) {

        database.execSQL(CREATE_SEARCHSPLOIT_TABLE);
        Toast.makeText(context.getApplicationContext(), "TABLE CREATED",
                Toast.LENGTH_LONG).show();
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + SearchSploit.TABLE);
        onCreate(database);
    }

    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }

    public void doDrop(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SearchSploit.TABLE);

    }

    public Boolean doDbFeed() {

        String data = nh.APP_PATH;
        String currentDBPath = "../databases/"  + "SearchSploit";
        File databasefile = new File(data, currentDBPath);
        File chrootfile = new File(CSVfileName);

        SQLiteDatabase db = this.getWritableDatabase();

        if (chrootfile.exists()) {
            Log.d(TAG, "files.csv found in chroot " + CSVfileName );
            if (databasefile.exists()) {
                // If database exists, remove old table and create new one
                doDrop();
                db.execSQL(CREATE_SEARCHSPLOIT_TABLE);
            }
            FileReader file;
            BufferedReader sqlBuffer = null;
            try {
                file = new FileReader(CSVfileName);
                sqlBuffer = new BufferedReader(file);
                sqlBuffer.readLine(); // Reads the first line (header) and throws away
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            String columns = SearchSploit.ID + ", " +
                    SearchSploit.FILE+ ", " +
                    SearchSploit.DESCRIPTION + ", " +
                    SearchSploit.DATE + ", " +
                    SearchSploit.AUTHOR + ", " +
                    SearchSploit.PLATFORM + ", " +
                    SearchSploit.TYPE + ", " +
                    SearchSploit.PORT;

            String queryOpener = "INSERT INTO " + SearchSploit.TABLE + " (" + columns + ") values (";
            String queryCloser = ");";

            db.beginTransaction();
            try {
                if(sqlBuffer == null){
                    return false;
                }
                while ((line = sqlBuffer.readLine()) != null) {
                    StringBuilder query = new StringBuilder(queryOpener);
                    String[] rowData = line.split(",");

                    query.append(sqlEscapeString(rowData[0])).append(",");
                    query.append(sqlEscapeString(rowData[1])).append(",");

                    // escape single quotes in description
                    // && remove the wrapping quotes
                    String description = rowData[2].replaceAll("^\"|\"$", "");
                    query.append(sqlEscapeString(description)).append(",");

                    query.append(sqlEscapeString(rowData[3])).append(",");
                    //remove the wrapping quotes
                    String author = rowData[4].replaceAll("^\"|\"$", "");
                    query.append(sqlEscapeString(author)).append(",");

                    query.append(sqlEscapeString(rowData[5])).append(",");
                    query.append(sqlEscapeString(rowData[6])).append(",");
                    query.append(sqlEscapeString(rowData[7]));
                    query.append(queryCloser);
                    db.execSQL(query.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        }
        return false;
    }

    public List<SearchSploit> getAllExploits() {
        String query = "SELECT  * FROM " + SearchSploit.TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<SearchSploit> _List = createExploitList(cursor);
        db.close();
        return _List;
    }
    public List<SearchSploit> getAllExploitsFiltered(String filter) {
        String wildcard = "%" + filter + "%";
        String query = "SELECT * FROM " + SearchSploit.TABLE
                + " WHERE BTN_LABEL like ?" ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{wildcard});
        List<SearchSploit> _List = createExploitList(cursor);
        db.close();
        return _List;
    }
    public List<SearchSploit> getAllExploitsAtBoot() {
        String query = "SELECT * FROM " + CustomCommand.TABLE
                + " WHERE RUN_AT_BOOT = 1" ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<SearchSploit> _List = createExploitList(cursor);
        db.close();
        return _List;
    }
    private List<SearchSploit> createExploitList(Cursor cursor){
        List<SearchSploit> commandList = new LinkedList<>();
        if (cursor.moveToFirst()) {
            do {
                SearchSploit _exploit = new SearchSploit();
                _exploit.setId(cursor.getInt(0));                  // id
                _exploit.setFile(cursor.getString(1));             // file
                _exploit.setDescription(cursor.getString(2));      // desc
                _exploit.setDate(cursor.getString(3));             // date
                _exploit.setAuthor(cursor.getString(4));           // author
                _exploit.setPlatform(cursor.getString(5));         // platform
                _exploit.setType(cursor.getString(6));             // type
                _exploit.setPort(cursor.getInt(7));                // port
                commandList.add(_exploit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return commandList;
    }
}