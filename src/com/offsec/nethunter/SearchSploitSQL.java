package com.offsec.nethunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.offsec.nethunter.utils.NhPaths;

import java.util.LinkedList;
import java.util.List;

class SearchSploitSQLiteHelper extends SQLiteOpenHelper {
    NhPaths nh;
    Context context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ssploit.db";
    private static final String TAG = "SearchSploitSQL";
    private SearchSploitSQLiteHelper mDbHelper;
    private SQLiteDatabase mDb;

    public SearchSploitSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        nh = new NhPaths();
    }
    public void onCreate(SQLiteDatabase database) {
        String CREATE_SEARCHSPLOIT_TABLE =
                "CREATE TABLE ssploitdb ( _id INTEGER PRIMARY KEY, " +
                        "file TEXT,"+
                        "description TEXT,"+
                        "date DATE,"+
                        "author TEXT,"+
                        "platform TEXT,"+
                        "type TEXT,"+
                        "port INTEGER)";
        database.execSQL(CREATE_SEARCHSPLOIT_TABLE);
        Log.d(TAG,"ssploit.db Created");
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS ssploitdb");
        onCreate(database);
    }

    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }
/*
    public Cursor searchByInputText(String inputText) throws SQLException {

        String query = "SELECT _id FROM ssploitdb WHERE description LIKE '" + inputText + "';";

        Cursor mCursor = mDb.rawQuery(query,null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }
*/
}