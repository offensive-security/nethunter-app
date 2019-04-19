package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;

import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;

//import androidx.appcompat.widget.SearchView;

public class CustomCommandsFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "CustomCommandsFragment";
    private CustomCommandsSQL database;
    private Context mContext;
    private ListView commandListView;
    private CmdLoader commandAdapter;
    private List<CustomCommand> commandList;
    private String bootScriptPath;
    private String shebang;
    private String custom_commands_runlevel;
    private final ShellExecuter exe = new ShellExecuter();
    private NhPaths nh;

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
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        //this runs BEFORE the ui is available
        mContext = getActivity().getApplicationContext();
        nh = new NhPaths();
        database = new CustomCommandsSQL(mContext);
        if (!sharedpreferences.contains("initial_commands")) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("initial_commands", "added");
            editor.apply();
            setUpInitialCommands();
        }

        bootScriptPath = nh.APP_INITD_PATH;
        shebang = "#!/system/bin/sh\n\n# Run at boot CustomCommand: ";
        custom_commands_runlevel = "90";

        View rootView = inflater.inflate(R.layout.custom_commands, container, false);
        final Button addCommand = rootView.findViewById(R.id.addCommand);
        setHasOptionsMenu(true);
        final SearchView searchStr = rootView.findViewById(R.id.searchCommand);
        main(rootView);
        // set up listeners
        addCommand.setOnClickListener(v -> showCommandDialog("ADD", null, 0));

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

    private void addToBoot(CustomCommand command) {
        String _label = command.getCommand_label();
        String _cmd = command.getCommand();
        //String _mode = command.getExec_Mode();
        String _sendTo = command.getSend_To_Shell();
        nh = new NhPaths();

        String composedCommand;
        if (_sendTo.equals("KALI")) {
            composedCommand = "su -c '"+nh.APP_SCRIPTS_PATH+"/bootkali custom_cmd " + _cmd + "'";
        } else {
            // SEND TO ANDROID
            // no sure, if we add su -c , we cant exec comands as a normal android user
            composedCommand = _cmd;
        }
        String bootServiceFile = bootScriptPath + "/" + custom_commands_runlevel + "_" + command.getId() + "_custom_command";
        String fileContents = shebang + _label + "\n" + composedCommand;
        Log.d("bootScript", fileContents);
        exe.RunAsRoot(new String[]{
                "cat > " + bootServiceFile + " <<s0133717hur75\n" + fileContents + "\ns0133717hur75\n",
                "chmod 700 " + bootServiceFile
        });

        // return the number of services

    }

    private void removeFromBoot(long commandId) {
        // return the number of services
        String bootServiceFile = bootScriptPath + "/" + custom_commands_runlevel + "_" + commandId + "_custom_command";
        exe.RunAsRoot(new String[]{"rm -rf " + bootServiceFile});
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
                    if (cc.getRun_At_Boot() == 1) {
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

        commandListView = rootView.findViewById(R.id.commandList);
        TextView customComandsInfo = rootView.findViewById(R.id.customComandsInfo);
        commandList = database.getAllCommands();
        commandAdapter = new CmdLoader(mContext, commandList);


        if (commandAdapter.getCount() == 0) {
            customComandsInfo.setText("Add a new command");
        }

        commandListView.setAdapter(commandAdapter);
        commandListView.setOnItemLongClickListener((parent, view, position, id) -> {
            ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

            CustomCommand currenCommand = (CustomCommand) commandListView.getItemAtPosition(position);
            showCommandDialog("EDIT", currenCommand, position);

            return false;
        });

    }

    private static void hideSoftKeyboard(final View caller) {
        caller.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) caller.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(caller.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                (dialog, id) -> {
                    dialog.cancel();
                    hideSoftKeyboard(getView());
                });

        final Spinner command_exec_mode = promptsView.findViewById(R.id.spinnerExecMode);
        final CheckBox run_at_boot = promptsView.findViewById(R.id.custom_comands_runAtBoot);

        run_at_boot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                command_exec_mode.setSelection(0);
                command_exec_mode.setEnabled(false);
            } else {
                command_exec_mode.setEnabled(true);
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

        final EditText userInputBtnLabel = promptsView.findViewById(R.id.editText_launcher_btn_label);
        final EditText userInputCommand = promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = promptsView.findViewById(R.id.spinnerRun_in_shell);
        final CheckBox run_at_boot = promptsView.findViewById(R.id.custom_comands_runAtBoot);
        alertDialogBuilder
                .setPositiveButton("OK",
                        (dialog, id) -> {
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
                                nh.showMessage("Command created.");

                                if (_run_at_boot == 1) {
                                    addToBoot(_insertedCommand);
                                }
                                // add to top of the list
                                commandList.add(0, _insertedCommand);
                                commandAdapter.notifyDataSetChanged();
                            } else {
                                nh.showMessage(getString(R.string.toast_input_error_launcher));
                            }
                            hideSoftKeyboard(getView());
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void editCommand(AlertDialog.Builder alertDialogBuilder, View promptsView, CustomCommand commandInfo, final int position) {

        final EditText userInputCommandLabel = promptsView.findViewById(R.id.editText_launcher_btn_label);
        final EditText userInputCommand = promptsView.findViewById(R.id.editText_launcher_command);
        final Spinner command_exec_mode = promptsView.findViewById(R.id.spinnerExecMode);
        final Spinner command_run_in_shell = promptsView.findViewById(R.id.spinnerRun_in_shell);
        final CheckBox run_at_boot = promptsView.findViewById(R.id.custom_comands_runAtBoot);
        // command Info
        final long _id = commandInfo.getId();
        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();
        Integer _runAtBoot = commandInfo.getRun_At_Boot();
        userInputCommandLabel.setText(_label);
        userInputCommand.setText(_cmd);

        if (_runAtBoot == 1) {
            run_at_boot.setChecked(true);
            command_exec_mode.setSelection(0); // allways background
            command_exec_mode.setEnabled(false); // force option 1
        }

        if (_sendTo.equals("KALI")) {
            command_run_in_shell.setSelection(0);
        } else {
            // android
            command_run_in_shell.setSelection(1);
        }
        if (_mode.equals("BACKGROUND")) {
            command_exec_mode.setSelection(0);
        } else {
            // interactive
            command_exec_mode.setSelection(1);
        }
        alertDialogBuilder
                .setPositiveButton("Update",
                        (dialog, id) -> {

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
                                nh.showMessage("Command Updated");
                                commandList.set(position, _updatedCommand);
                                commandAdapter.notifyDataSetChanged();

                            } else {
                                nh.showMessage(getString(R.string.toast_input_error_launcher));
                            }
                            hideSoftKeyboard(getView());
                        })
                .setNeutralButton("Delete",
                        (dialog, id) -> {
                            database.deleteCommand(_id);
                            removeFromBoot(_id);
                            commandList.remove(position);
                            commandAdapter.notifyDataSetChanged();
                            hideSoftKeyboard(getView());
                            nh.showMessage("Command Deleted");
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setUpInitialCommands() {
        database.addCommand("Update Kali metapackages", nh.makeTermTitle("Updating Kali") + "apt-get update && apt-get upgrade", "INTERACTIVE", "KALI", 0);
        database.addCommand("Wlan1 Monitor Mode", nh.makeTermTitle("Wlan1 Monitor UP") + "sudo ifconfig wlan1 down && sudo iwconfig wlan1 mode monitor && sudo ifconfig wlan1 up && echo \"wlan1 Monitor mode enabled\" && sleep 3 && exit", "INTERACTIVE", "KALI", 0);
        database.addCommand("Launch Wifite", nh.makeTermTitle("Wifite") + "wifite", "INTERACTIVE", "KALI", 0);
        database.addCommand("Dump Mifare", nh.makeTermTitle("DumpMifare") + "dumpmifare.sh", "INTERACTIVE", "KALI", 0);
        database.addCommand("Backup Kali Chroot", nh.makeTermTitle("Backup_Kali_Chroot") + "su --mount-master -c 'chroot_backup /data/local/nhsystem/kali-armhf /sdcard/kalifs-backup.tar.gz'",
                "INTERACTIVE", "ANDROID", 0);
    }
}

class CmdLoader extends BaseAdapter {

    private final List<CustomCommand> _commandList;
    private final Context _mContext;

    private final ShellExecuter exe = new ShellExecuter();


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
            vH.cwTitle = convertView.findViewById(R.id.command_tag);
            // vH.cwSwich = (Switch) convertView.findViewById(R.id.switch1);
            vH.execmode = convertView.findViewById(R.id.execmode);
            vH.sendtocmd = convertView.findViewById(R.id.sendtocmd);
            vH.runatboot = convertView.findViewById(R.id.custom_comands_runAtBoot_text);
            vH.cwButton = convertView.findViewById(R.id.runCommand);
            convertView.setTag(vH);
            //System.out.println ("created row");
        } else {
            // recycle the items in the list if already exists
            vH = (ViewHolderItem) convertView.getTag();
        }

        // remove listeners
        final CustomCommand commandInfo = getItem(position);
        String _label = commandInfo.getCommand_label();
        // String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();
        Integer _runAtBoot = commandInfo.getRun_At_Boot();
        String _runAtBoot_txt = "NO";
        if (_runAtBoot == 1) {
            _runAtBoot_txt = "YES";
            vH.runatboot.setTextColor(_mContext.getResources().getColor(R.color.darkorange));
        } else {
            vH.runatboot.setTextColor(_mContext.getResources().getColor(R.color.blue));
        }
        vH.cwButton.setOnClickListener(null);
        // set service name
        vH.cwTitle.setText(_label);
        vH.execmode.setText(_mode);
        vH.sendtocmd.setText(_sendTo);
        vH.runatboot.setText(_runAtBoot_txt);
        vH.cwButton.setOnClickListener(v -> doCustomCommand(commandInfo));
        return convertView;

    }

    public CustomCommand getItem(int position) {
        return _commandList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private boolean checkTerminalExternalPermission(String permission) {
        int res = _mContext.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void doCustomCommand(CustomCommand commandInfo) {

        String _label = commandInfo.getCommand_label();
        String _cmd = commandInfo.getCommand();
        String _mode = commandInfo.getExec_Mode();
        String _sendTo = commandInfo.getSend_To_Shell();
        String composedCommand;

        if (_mode.equals("BACKGROUND")) {
            if (_sendTo.equals("KALI")) {
                new BootKali(_cmd).run_bg();
                Toast.makeText(_mContext,
                        "Kali cmd done.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // dont run all the bg commands as root
                exe.Executer(_cmd);
                Toast.makeText(_mContext,
                        "Android cmd done.",
                        Toast.LENGTH_SHORT).show();
            }
        } else try {
            // INTERACTIVE
            if (_sendTo.equals("KALI")) {
                Intent intent =
                        new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("com.offsec.nhterm.iInitialCommand", _cmd);
                _mContext.startActivity(intent);

            } else {
                Intent intent =
                        new Intent("com.offsec.nhterm.RUN_SCRIPT");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("com.offsec.nhterm.iInitialCommand", _cmd);
                _mContext.startActivity(intent);


            }

        } catch (Exception e) {
            if (!checkTerminalExternalPermission("com.offsec.nhterm.permission.RUN_SCRIPT_NH") ||
                    !checkTerminalExternalPermission("com.offsec.nhterm.permission.RUN_SCRIPT")) {
                Toast.makeText(_mContext, _mContext.getString(R.string.toast_error_permissions), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(_mContext, _mContext.getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
