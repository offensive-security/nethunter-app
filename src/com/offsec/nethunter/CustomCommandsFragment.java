package com.offsec.nethunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private List<CustomCommand> commandList;
    private String bootScriptPath;
    private String shebang;
    private String custom_commands_runlevel;
    private ShellExecuter exe = new ShellExecuter();

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

        View rootView = inflater.inflate(R.layout.custom_commands, container, false);
        final Button addCommand = (Button) rootView.findViewById(R.id.addCommand);
        setHasOptionsMenu(true);
        final SearchView searchStr= (SearchView) rootView.findViewById(R.id.searchCommand);
        main(rootView);
        // set up listeners
        addCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommandDialog("ADD", null, 0);
            }
        });

        searchStr.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                commandList.clear();
                commandList.addAll(database.getAllCommandsFiltered(query));
                commandAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
        return rootView;

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //this runs BEFORE the ui is available
        mContext = getActivity().getApplicationContext();
        database = new SQLPersistence(mContext);
        bootScriptPath = mContext.getFilesDir().toString() + "/etc/init.d/";
        shebang = "#!/system/bin/sh\n\n# Run at boot CustomCommand: ";
        custom_commands_runlevel = "90";

    }
    private void addToBoot(CustomCommand command) {
        String _label = command.getCommand_label();
        String _cmd = command.getCommand();
        //String _mode = command.getExec_Mode();
        String _sendTo = command.getSend_To_Shell();

        String composedCommand;
        if(_sendTo.equals("KALI")){
            composedCommand = "su -c bootkali custom_cmd " + _cmd;
        } else{
            // SEND TO ANDROID
            // no sure, if we add su -c , we cant exec comands as a normal android user
            composedCommand = _cmd;
        }
        String bootServiceFile = bootScriptPath + custom_commands_runlevel + "_" + command.getId() +"_custom_command";
        String fileContents = shebang + _label + "\n" + composedCommand;
        exe.RunAsRoot(new String[]{
                "echo '" + fileContents + "' > " + bootServiceFile,
                "chmod 700 " + bootServiceFile
        });

        // return the number of services

    }
    private void removeFromBoot(long commandId) {
        // return the number of services
        String bootServiceFile = bootScriptPath + custom_commands_runlevel + "_" + commandId +"_custom_command";
        exe.RunAsRoot(new String[]{"rm -rf " + bootServiceFile});
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
                hideSoftKeyboard(getView());
                return true;
            case R.id.doDbRestore:
                // delete boot coomands
                for (CustomCommand cc : database.getAllCommandsAtBoot()) {
                        removeFromBoot(cc.getId());
                }
                //restore db
                database.importDB();
                commandList.clear();
                // restored list
                commandList.addAll(database.getAllCommands());
                commandAdapter.notifyDataSetChanged();
                // restore boot commands
                for (CustomCommand cc : database.getAllCommandsAtBoot()) {
                    if(cc.getRun_At_Boot() == 1) {
                        addToBoot(cc);
                    }
                }
                hideSoftKeyboard(getView());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void main(final View rootView) {

        commandListView = (ListView) rootView.findViewById(R.id.commandList);
        TextView customComandsInfo = (TextView) rootView.findViewById(R.id.customComandsInfo);
        commandList = database.getAllCommands();
        commandAdapter = new CmdLoader(mContext, commandList);


        if(commandAdapter.getCount() == 0){
            customComandsInfo.setText("Add a new command");
        }

        commandListView.setAdapter(commandAdapter);
        commandListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

                CustomCommand currenCommand = (CustomCommand) commandListView.getItemAtPosition(position);
                showCommandDialog("EDIT", currenCommand, position);

                return false;
            }
        });

    }

    private static void hideSoftKeyboard(final View caller) {
        caller.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) caller.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(caller.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 100);
    }
    private void showCommandDialog(String action, CustomCommand commandInfo, int position) {
        // common setup
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptsView = inflater.inflate(R.layout.custon_commands_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        hideSoftKeyboard(getView());
                    }
                });

        final Spinner command_exec_mode = (Spinner) promptsView.findViewById(R.id.spinnerExecMode);
        final CheckBox run_at_boot = (CheckBox) promptsView.findViewById(R.id.custom_comands_runAtBoot);

        run_at_boot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    command_exec_mode.setSelection(0);
                    command_exec_mode.setEnabled(false);
                } else {
                    command_exec_mode.setEnabled(true);
                }
            }
        });
        switch (action) {
            case "ADD":
                saveNewCommand(alertDialogBuilder, promptsView);
                break;
            case "EDIT":
                editCommand(alertDialogBuilder, promptsView, commandInfo, position);
                break;
        }
    }
    private void saveNewCommand(AlertDialog.Builder alertDialogBuilder, View promptsView) {

        final EditText userInputBtnLabel= (EditText) promptsView.findViewById(R.id.editText_launcher_btn_label);
        final EditText userInputCommand = (EditText) promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = (Spinner) promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = (Spinner) promptsView.findViewById(R.id.spinnerRun_in_shell);
        final CheckBox run_at_boot = (CheckBox) promptsView.findViewById(R.id.custom_comands_runAtBoot);
        alertDialogBuilder
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userInputBtnLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {
                                    Integer _run_at_boot = 0;
                                    if (run_at_boot.isChecked()) {
                                        _run_at_boot = 1;

                                    }
                                    CustomCommand _insertedCommand = database.addCommand(userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString(),
                                            command_exec_mode.getSelectedItem().toString(),
                                            command_run_in_shell.getSelectedItem().toString(), _run_at_boot);
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "Command created.",
                                            Toast.LENGTH_SHORT).show();

                                    if (_run_at_boot == 1) {
                                        addToBoot(_insertedCommand);
                                    }
                                    // add to top of the list
                                    commandList.add(0, _insertedCommand);
                                    commandAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            getString(R.string.toast_input_error_launcher),
                                            Toast.LENGTH_SHORT).show();
                                }
                                hideSoftKeyboard(getView());
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void editCommand(AlertDialog.Builder alertDialogBuilder, View promptsView, CustomCommand commandInfo, final int position) {

        final EditText userInputCommandLabel= (EditText) promptsView.findViewById(R.id.editText_launcher_btn_label);
        final EditText userInputCommand = (EditText) promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = (Spinner) promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = (Spinner) promptsView.findViewById(R.id.spinnerRun_in_shell);
        final CheckBox run_at_boot = (CheckBox) promptsView.findViewById(R.id.custom_comands_runAtBoot);
        // command Info
        final long _id = commandInfo.getId();
        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();
        Integer _runAtBoot = commandInfo.getRun_At_Boot();
        userInputCommandLabel.setText(_label);
        userInputCommand.setText(_cmd);

        if(_runAtBoot == 1){
            run_at_boot.setChecked(true);
            command_exec_mode.setSelection(0); // allways background
            command_exec_mode.setEnabled(false); // force option 1
        }

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
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (userInputCommandLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {
                                    Integer _run_at_boot = 0;
                                    if (run_at_boot.isChecked()) {
                                        _run_at_boot = 1;
                                    }
                                    CustomCommand _updatedCommand = new CustomCommand(_id,
                                            userInputCommandLabel.getText().toString(),
                                            userInputCommand.getText().toString(),
                                            command_exec_mode.getSelectedItem().toString(),
                                            command_run_in_shell.getSelectedItem().toString(), _run_at_boot);

                                    database.updateCommand(_updatedCommand);
                                    if (_run_at_boot == 1) {
                                        addToBoot(_updatedCommand);
                                    } else {
                                        removeFromBoot(_updatedCommand.getId());
                                    }
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "Command Updated",
                                            Toast.LENGTH_SHORT).show();
                                    commandList.set(position, _updatedCommand);
                                    commandAdapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            getString(R.string.toast_input_error_launcher),
                                            Toast.LENGTH_SHORT).show();
                                }
                                hideSoftKeyboard(getView());
                            }
                        })
                .setNeutralButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.deleteCommand(_id);
                                removeFromBoot(_id);
                                commandList.remove(position);
                                commandAdapter.notifyDataSetChanged();
                                hideSoftKeyboard(getView());
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Command Deleted",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

class CmdLoader extends BaseAdapter {

    private final List<CustomCommand> _commandList;
    private Context _mContext;

    private ShellExecuter exe = new ShellExecuter();


    public CmdLoader(Context context, List<CustomCommand> commandList) {

        _mContext = context;
        _commandList = commandList;

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
            vH.runatboot = (TextView) convertView.findViewById(R.id.custom_comands_runAtBoot_text);
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
        // String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();
        Integer _runAtBoot = commandInfo.getRun_At_Boot();
        String _runAtBoot_txt = "NO";
        if (_runAtBoot ==1){
            _runAtBoot_txt = "YES";
            vH.runatboot.setTextColor(_mContext.getResources().getColor(R.color.darkorange));
        } else {
            vH.runatboot.setTextColor(_mContext.getResources().getColor(R.color.link_text_material_dark));
        }
        vH.cwButton.setOnClickListener(null);
        // set service name
        vH.cwTitle.setText(_label);
        vH.execmode.setText(_mode);
        vH.sendtocmd.setText(_sendTo);
        vH.runatboot.setText(_runAtBoot_txt);
        vH.cwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCustomCommand(commandInfo);
            }
        });
        return convertView;

    }

    public CustomCommand getItem(int position) {
        return _commandList.get(position);
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
            // no sure, if we add su -c , we cant exec comands as a normal android user
            composedCommand = _cmd;
        }

        if(_mode.equals("BACKGROUND")){
            // dont run all the bg commands as root
            exe.Executer(composedCommand);
            Toast.makeText(_mContext,
                    "Command " + _label + " done.",
                    Toast.LENGTH_SHORT).show();
        } else try {
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
