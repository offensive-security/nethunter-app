/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;

public class SQLPersistence extends SQLiteOpenHelper {

    final static int DATABASE_VERSION = 1;
    final static String DATABASE_NAME = "KaliLaunchers";

    final static String CREATE_LAUNCHER_TABLE = "CREATE TABLE " +
            LauncherApp.TABLE + " (" +
            LauncherApp.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LauncherApp.BTN_LABEL + " TEXT, " +
            LauncherApp.CMD + " TEXT )";

    public SQLPersistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LAUNCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS");
        this.onCreate(db);
    }

    public long addApp(final String btn_name, final String command) {
        long id = 0;
        if (btn_name.length() > 0 &&
                command.length() > 0) {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(LauncherApp.BTN_LABEL, btn_name);
            values.put(LauncherApp.CMD, command);

            id = db.insert(LauncherApp.TABLE, null, values);
            db.close();
        }
        return id;
    }

    public List<LauncherApp> getAllApps() {
        List<LauncherApp> apps = new LinkedList<LauncherApp>();
        String query = "SELECT  * FROM " + LauncherApp.TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        LauncherApp app = null;
        if (cursor.moveToFirst()) {
            do {
                app = new LauncherApp();
                app.setId(Long.parseLong(cursor.getString(0)));
                app.setBtn_label(cursor.getString(1));
                app.setCommand(cursor.getString(2));
                apps.add(app);
            } while (cursor.moveToNext());
        }
        return apps;
    }


    public LauncherApp getApp(final long id) {
        LauncherApp app = null;
        if (id != 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor =
                    db.query(LauncherApp.TABLE,
                            LauncherApp.COLUMNS,
                            " id = ?",
                            new String[]{String.valueOf(id)},
                            null,
                            null,
                            null,
                            null);

            if (cursor != null)
                cursor.moveToFirst();

            app = new LauncherApp();
            app.setId(Long.parseLong(cursor.getString(0)));
            app.setBtn_label(cursor.getString(1));
            app.setCommand(cursor.getString(2));
        }
        return app;
    }

    public void updateApp(final LauncherApp app) {
        if (app != null) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(LauncherApp.BTN_LABEL, app.getBtn_label());
            values.put(LauncherApp.CMD, app.getCommand());

            db.update(LauncherApp.TABLE, values,
                    LauncherApp.ID + " = ?",
                    new String[]{String.valueOf(app.getId())});

            db.close();
        }
    }

    public void deleteApp(final long id) {
        if (id != 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(LauncherApp.TABLE, LauncherApp.ID + " = ?",
                    new String[]{String.valueOf(id)});

            db.close();
        }
    }
}
