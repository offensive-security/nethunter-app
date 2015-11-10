package com.offsec.nethunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CustomCommandsFragment  extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "CustomCommandsFragment";
    private SQLPersistence database;
    private Context mContext;
    private ListView commandListView;
    private CmdLoader commandAdapter;
    private List commandList;

    public CustomCommandsFragment() {

    }
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CustomCommandsFragment newInstance(int sectionNumber) {

        CustomCommandsFragment fragment = new CustomCommandsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity().getApplicationContext();
        database = new SQLPersistence(mContext);

        View rootView = inflater.inflate(R.layout.custom_commands, container, false);
        final Button addCommand = (Button) rootView.findViewById(R.id.addCommand);
        setHasOptionsMenu(true);
        final SearchView searchStr= (SearchView) rootView.findViewById(R.id.searchCommand);
        addCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewCommand();
            }
        });
        searchStr.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                addCommand.setVisibility(View.VISIBLE);
                commandList = database.getAllCommands();
                commandAdapter = new CmdLoader(mContext, commandList);
                commandListView.setAdapter(new CmdLoader(mContext, commandList));

                return false;
            }
        });
        searchStr.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                commandList = database.getAllAppsFiltered(query);
                commandListView.setAdapter(new CmdLoader(mContext, commandList));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
        main(rootView);

        return rootView;

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onResume()
    {
        super.onResume();
    }

    public void onPause()
    {
        super.onPause();
    }

    public void onStop()
    {
        super.onStop();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.custom_commands, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.doDbBackup:
                database.exportDB();
                main(null);
                return true;
            case R.id.doDbRestore:
                database.importDB();
                main(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void main(final View rootView) {
        if(rootView != null){
            commandListView = (ListView) rootView.findViewById(R.id.commandList);
        }
        commandList = database.getAllCommands();
        commandAdapter = new CmdLoader(mContext, commandList);
        if(commandAdapter.getCount() == 0){
            TextView customComandsInfo = (TextView)rootView.findViewById(R.id.customComandsInfo);
            customComandsInfo.setText("Add a new command");
            return;
        }
        commandListView.setAdapter(commandAdapter);
        commandListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

                CustomCommand currenCommand = (CustomCommand) commandListView.getItemAtPosition(position);
                editCommand(currenCommand);

                return false;
            }
        });

    }

    private void saveNewCommand() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptsView = inflater.inflate(R.layout.custon_commands_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);

        final EditText userInputBtnLabel= (EditText) promptsView.findViewById(R.id.editText_launcher_btn_label);

        final EditText userInputCommand = (EditText) promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = (Spinner) promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = (Spinner) promptsView.findViewById(R.id.spinnerRun_in_shell);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userInputBtnLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {

                                    database.addCommand(userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString(),
                                            command_exec_mode.getSelectedItem().toString(),
                                            command_run_in_shell.getSelectedItem().toString());
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "Command created.",
                                            Toast.LENGTH_SHORT).show();
                                    commandList = database.getAllCommands();
                                    commandAdapter = new CmdLoader(mContext, commandList);
                                    commandListView.setAdapter(commandAdapter);

                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            getString(R.string.toast_input_error_launcher),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void editCommand(final CustomCommand commandInfo) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = inflater.inflate(R.layout.custon_commands_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(promptsView);

        final EditText userInputCommandLabel= (EditText) promptsView.findViewById(R.id.editText_launcher_btn_label);
        final EditText userInputCommand = (EditText) promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = (Spinner) promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = (Spinner) promptsView.findViewById(R.id.spinnerRun_in_shell);

        // command Info
        final Long _id = commandInfo.getId();
        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();

        userInputCommandLabel.setText(_label);
        userInputCommand.setText(_cmd);

        if(_sendTo.equals("KALI")){
            command_run_in_shell.setSelection(0);
        } else {
            // android
            command_run_in_shell.setSelection(1);
        }
        if(_mode.equals("BACKGROUND")){
            command_exec_mode.setSelection(0);
        } else {
            // interactive
            command_exec_mode.setSelection(1);
        }

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userInputCommandLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {


                                    CustomCommand commandObj = new CustomCommand(_id,
                                            userInputCommandLabel.getText().toString(),
                                            userInputCommand.getText().toString(),
                                            command_exec_mode.getSelectedItem().toString(),
                                            command_run_in_shell.getSelectedItem().toString());
                                    database.updateCommand(commandObj);
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "Command Updated",
                                            Toast.LENGTH_SHORT).show();
                                    commandList = database.getAllCommands();
                                    commandAdapter = new CmdLoader(mContext, commandList);
                                    commandListView.setAdapter(commandAdapter);

                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            getString(R.string.toast_input_error_launcher),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNeutralButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.deleteCommand(_id);
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Command Deleted",
                                        Toast.LENGTH_SHORT).show();
                                commandList = database.getAllCommands();
                                commandAdapter = new CmdLoader(mContext, commandList);
                                commandListView.setAdapter(commandAdapter);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

class CmdLoader extends BaseAdapter {


    private final List _commandList;
    private Context _mContext;

    private ShellExecuter exe = new ShellExecuter();


    public CmdLoader(Context context, List commandList) {

        _mContext = context;
        _commandList = commandList;

    }

    static class ViewHolderItem {
        // The switch
        //Switch sw;
        // the msg holder
        TextView execmode;
        TextView sendtocmd;
        // the service title
        TextView cwTitle;
        // run at boot checkbox
        Button cwButton;
    }

    public int getCount() {
        // return the number of services
        return _commandList.size();
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View convertView, ViewGroup parent) {
        // inflate the layout for each item of listView (our services)

        ViewHolderItem vH;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_commands_item, parent, false);

            // set up the ViewHolder
            vH = new ViewHolderItem();
            // get the reference of switch and the text view
            vH.cwTitle = (TextView) convertView.findViewById(R.id.command_tag);
            // vH.cwSwich = (Switch) convertView.findViewById(R.id.switch1);
            vH.execmode = (TextView) convertView.findViewById(R.id.execmode);
            vH.sendtocmd = (TextView) convertView.findViewById(R.id.sendtocmd);
            vH.cwButton = (Button) convertView.findViewById(R.id.runCommand);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list if already exists
            vH = (ViewHolderItem) convertView.getTag();
        }
        if (position >= _commandList.size()) {
            // out of range, return ,do nothing
            return convertView;
        }
        // remove listeners
        final CustomCommand commandInfo = getItem(position);
        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();

        vH.cwButton.setOnClickListener(null);
        // set service name
        vH.cwTitle.setText(_label);
        vH.execmode.setText(_mode);
        vH.sendtocmd.setText(_sendTo);
        vH.cwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCustomCommand(commandInfo);
            }
        });
        return convertView;

    }

    public CustomCommand getItem(int position) {
        return (CustomCommand)_commandList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private void doCustomCommand(CustomCommand commandInfo){

        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();

        String composedCommand;
        if(_sendTo.equals("KALI")){
            composedCommand = "su -c bootkali custom_cmd " + _cmd;
        } else{
            // SEND TO ANDROID
            composedCommand = _cmd;
        }

        if(_mode.equals("BACKGROUND")){
            exe.RunAsRoot(new String[]{composedCommand});
            Toast.makeText(_mContext,
                    "Command " + _label + " done.",
                    Toast.LENGTH_SHORT).show();
        } else {
            try {
                Intent intent =
                        new Intent("jackpal.androidterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.putExtra("jackpal.androidterm.iInitialCommand", composedCommand);
                _mContext.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(_mContext, _mContext.getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
                try {
                    _mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=jackpal.androidterm")));
                } catch (android.content.ActivityNotFoundException anfe2) {
                    _mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jackpal.androidterm")));
                }
            }
        }



    }
}
