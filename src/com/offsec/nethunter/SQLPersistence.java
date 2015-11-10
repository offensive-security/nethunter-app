/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class SQLPersistence extends SQLiteOpenHelper {

    final static int DATABASE_VERSION = 1;
    final static String DATABASE_NAME = "KaliLaunchers";

    final static String CREATE_LAUNCHER_TABLE = "CREATE TABLE " +
            CustomCommand.TABLE + " (" +
            CustomCommand.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CustomCommand.BTN_LABEL + " TEXT, " +
            CustomCommand.CMD + " TEXT, " +
            CustomCommand.EXEC_MODE + " TEXT, " +
            CustomCommand.SEND_TO_SHELL + " TEXT )";
    Context context;
    public SQLPersistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    public long addCommand(final String command_tag, final String command, final String mode, final String send_to_shell) {
        long id = 0;
        if (command_tag.length() > 0 &&
                command.length() > 0) {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(CustomCommand.BTN_LABEL, command_tag);
            values.put(CustomCommand.CMD, command);
            values.put(CustomCommand.EXEC_MODE, mode);
            values.put(CustomCommand.SEND_TO_SHELL, send_to_shell);

            id = db.insert(CustomCommand.TABLE, null, values);
            db.close();
        }
        return id;
    }
    public void updateCommand(final CustomCommand updatedCommand) {
        if (updatedCommand != null) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(CustomCommand.BTN_LABEL, updatedCommand.getCommand_label());
            values.put(CustomCommand.CMD, updatedCommand.getCommand());
            values.put(CustomCommand.EXEC_MODE, updatedCommand.getExec_Mode());
            values.put(CustomCommand.SEND_TO_SHELL, updatedCommand.getSend_To_Shell());
            db.update(CustomCommand.TABLE, values,
                    CustomCommand.ID + " = ?",
                    new String[]{String.valueOf(updatedCommand.getId())});

            db.close();
        }
    }

    public void deleteCommand(final long id) {
        if (id != 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(CustomCommand.TABLE,
                    CustomCommand.ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();

        }
    }
    public List<CustomCommand> getAllCommands() {
        String query = "SELECT  * FROM " + CustomCommand.TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return createCommandList(cursor);
    }
    public List<CustomCommand> getAllAppsFiltered(String filter) {
        String wildcard = "%" + filter + "%";
        String query = "SELECT * FROM " + CustomCommand.TABLE
                + " WHERE BTN_LABEL like ?" ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{wildcard});
        return createCommandList(cursor);
    }

    private List<CustomCommand> createCommandList(Cursor cursor){

        List<CustomCommand> commandList = new LinkedList<>();
        if (cursor.moveToFirst()) {
            do {
                CustomCommand _command = new CustomCommand();

                _command.setId(Long.parseLong(cursor.getString(0))); // id
                _command.setBtn_label(cursor.getString(1));          // tag
                _command.setCommand(cursor.getString(2));            // command
                _command.setExec_Mode(cursor.getString(3));          // mode
                _command.setSend_To_Shell(cursor.getString(4));      // run in shell
                commandList.add(_command);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return commandList;

    }
    public void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = context.getFilesDir();
            if (sd.canWrite()) {
                String currentDBPath = "../databases/"  + DATABASE_NAME;
                String backupDBPath = "/nh_bak_" + DATABASE_NAME + "_" + DATABASE_VERSION; // From SD directory.
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d("importDB", "Successful");

            }
        } catch (Exception e) {
            Log.d("importDB", e.toString());
        }
    }

    public void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = context.getFilesDir();
            Log.d("ExportDB sd =", sd.toString());
            Log.d("ExportDB sd =", data.toString());
            if (sd.canWrite()) {
                String currentDBPath = "../databases/"  + DATABASE_NAME;
                String backupDBPath = "/nh_bak_" + DATABASE_NAME + "_" + DATABASE_VERSION;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d("ExportDB", "Successful");

            }
        } catch (Exception e) {
            Log.d("ExportDB", "ExportDB Failed!");
        }
    }
}