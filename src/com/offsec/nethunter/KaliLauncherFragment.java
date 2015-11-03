package com.offsec.nethunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.List;

//import android.app.Fragment;

public class KaliLauncherFragment extends Fragment {

    private SQLPersistence database;
    private LayoutInflater inflater;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */


    public KaliLauncherFragment() {
    }

    public static KaliLauncherFragment newInstance(int sectionNumber) {
        KaliLauncherFragment fragment = new KaliLauncherFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        database = new SQLPersistence(getActivity().getApplicationContext());
        this.inflater = inflater;

        View rootView = inflater.inflate(R.layout.kali_launcher, container, false);
        addClickListener(R.id.button_start_kali, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali");
            }
        }, rootView);
        /**
         * Launch Kali menu
         */
        addClickListener(R.id.button_start_kalimenu, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali kalimenu");
            }
        }, rootView);
        /**
         * Update Kali chroot
         */
        addClickListener(R.id.update_kali_chroot, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali update");
            }
        }, rootView);
        /**
         * Launch Wifite
         */
        addClickListener(R.id.button_launch_wifite, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali wifite");
            }
        }, rootView);
        /**
         * Turn off external wifi
         */
        addClickListener(R.id.turn_off_external_wifi, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali wifi-disable");
            }
        }, rootView);
        /**
         * Turn off external wifi
         */
        addClickListener(R.id.kali_dumpmifare, new View.OnClickListener() {
            public void onClick(View v) {
                intentClickListener("bootkali dumpmifare");
            }
        }, rootView);
        /**
         * Shutdown Kali
         */
        addClickListener(R.id.shutdown_kali, new View.OnClickListener() {
            public void onClick(View v) {
            intentClickListener("killkali");
            }
        }, rootView);
        /**
         * Add button
         */
        addClickListener(R.id.add_Button, new View.OnClickListener() {
            public void onClick(View v) {
                createNewButton();
            }
        }, rootView);

        // load buttons from db
        List<LauncherApp> apps = database.getAllApps();
        for (LauncherApp app : apps) {
            LauncherButton newButton = createButton(app.getBtn_label(), app.getCommand(), app.getId());
            if (newButton != null) {
                LinearLayout mainLayout = (LinearLayout) rootView.findViewById(R.id.launcher_layout);
                mainLayout.addView(newButton);
            }
        }
        return rootView;
    }

    public void getTerminalApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=jackpal.androidterm")));
        } catch (android.content.ActivityNotFoundException anfe2) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jackpal.androidterm")));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void addClickListener(int buttonId, View.OnClickListener onClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnClickListener(onClickListener);
    }

    private void addLongClickListener(int buttonId, View.OnLongClickListener onLongClickListener, View rootView) {
        rootView.findViewById(buttonId).setOnLongClickListener(onLongClickListener);
    }

    private LauncherButton createButton(final String label, final String command, final long id) {

        if (label.length() > 0 && command.length() > 0 && id != 0) {
            final LauncherButton newButton = new LauncherButton(getActivity().getApplicationContext());

            newButton.setWidth(LinearLayout.LayoutParams.FILL_PARENT);
            newButton.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            newButton.setText(label);
            newButton.setDb_id(id);
            newButton.setTextColor(Color.LTGRAY);
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intentClickListener("bootkali custom_cmd " + command);
                }
            });

            newButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View view) {
                    deleteUpdateButton(newButton);
                    return true;
                }
            });
            return newButton;
        }
        return null;
    }

    private void deleteButton(final LauncherButton button) {
        if (button != null) {
            LinearLayout mainLayout = (LinearLayout) getActivity().findViewById(R.id.launcher_layout);
            button.setOnLongClickListener(null);
            button.setOnClickListener(null);
            mainLayout.removeView(button);
        }
    }

    private void createNewButton() {

        View promptsView = inflater.inflate(R.layout.newapplauncher, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(promptsView);

        final EditText userInputBtnLabel= (EditText) promptsView
                .findViewById(R.id.editText_launcher_btn_label);

        final EditText userInputCommand = (EditText) promptsView
                .findViewById(R.id.editText_launcher_command);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userInputBtnLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {

                                    long db_id = database.addApp(userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString());

                                    LauncherButton newButton = createButton(userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString(), db_id);

                                    if (newButton != null) {
                                        LinearLayout mainLayout = (LinearLayout) getActivity().findViewById(R.id.launcher_layout);
                                        mainLayout.addView(newButton);
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                getString(R.string.toast_error_launcher),
                                                Toast.LENGTH_SHORT).show();
                                    }

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

    private void deleteUpdateButton(final LauncherButton button) {

        final LauncherApp app = database.getApp(button.getDb_id());

        View promptsView = inflater.inflate(R.layout.newapplauncher, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(promptsView);

        final EditText userInputBtnLabel= (EditText) promptsView
                .findViewById(R.id.editText_launcher_btn_label);
        userInputBtnLabel.setText(app.getBtn_label());

        final EditText userInputCommand = (EditText) promptsView
                .findViewById(R.id.editText_launcher_command);
        userInputCommand.setText(app.getCommand());

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userInputBtnLabel.getText().length() > 0 &&
                                        userInputCommand.getText().length() > 0) {

                                    database.updateApp(new LauncherApp(app.getId(),
                                            userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString()));

                                    deleteButton(button);

                                    LauncherButton newButton = createButton(userInputBtnLabel.getText().toString(),
                                            userInputCommand.getText().toString(), app.getId());

                                    if (newButton != null) {
                                        LinearLayout mainLayout = (LinearLayout) getActivity().findViewById(R.id.launcher_layout);
                                        mainLayout.addView(newButton);
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                getString(R.string.toast_error_launcher),
                                                Toast.LENGTH_SHORT).show();
                                    }

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
                                database.deleteApp(app.getId());
                                deleteButton(button);
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

    private void intentClickListener(final String command) {
        try {
            Intent intent =
                    new Intent("jackpal.androidterm.RUN_SCRIPT");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("jackpal.androidterm.iInitialCommand", "su -c " + command);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
            getTerminalApp();
        }
    }
}
