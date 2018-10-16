package com.offsec.nethunter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;


public class UsbArmyFragment extends Fragment {

    private static NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public UsbArmyFragment() {

    }

    public static UsbArmyFragment newInstance(int sectionNumber) {
        UsbArmyFragment fragment = new UsbArmyFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.usbarmy, container, false);
        nh = new NhPaths();
        final String image_folder_path = nh.APP_SD_FILES_PATH + "/img_folder";
        final String script_folder_path = nh.APP_SD_FILES_PATH + "/punkscripts";
        final Button setusbinterface = (Button) rootView.findViewById(R.id.setusbinterface);
        final Button mountImage = (Button) rootView.findViewById(R.id.mountImage);
        final Button unmountImage = (Button) rootView.findViewById(R.id.unmountImage);
        final Button scriptRunner = (Button) rootView.findViewById(R.id.scriptRunner);
        final ImageButton reloadUSB = (ImageButton) rootView.findViewById(R.id.reloadUSB);
        final ImageButton reloadImage = (ImageButton) rootView.findViewById(R.id.reloadImage);
        final Spinner usbTargetSpinner = (Spinner) rootView.findViewById(R.id.usb_targets);
        final Spinner usbModeSpinner = (Spinner) rootView.findViewById(R.id.usb_states);
        final Spinner usbAdbSpinner = (Spinner) rootView.findViewById(R.id.usb_adb);
        final Spinner imageSpinner = (Spinner) rootView.findViewById(R.id.usbarmy_img_mounter_preset_spinner);
        final Spinner scriptSpinner = (Spinner) rootView.findViewById(R.id.usbarmy_script_runner_preset_spinner);
        final TextView usbState = (TextView) rootView.findViewById(R.id.current_usb_state);
        final TextView imageState = (TextView) rootView.findViewById(R.id.mountedImage_state);
        final CheckBox ro_checkbox = (CheckBox) rootView.findViewById(R.id.usbarmy_ro);

        ro_checkbox.setChecked(false);

        refreshUSB(usbState);
        refreshImage(imageState);
        getImageFiles(imageSpinner);
        getScriptFiles(scriptSpinner);

        usbTargetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String items = usbTargetSpinner.getSelectedItem().toString();
                if (items.equals("Windows") || items.equals("Linux")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.usb_states_win_lin, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    usbModeSpinner.setAdapter(adapter);
                } else {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.usb_states_mac, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    usbModeSpinner.setAdapter(adapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        setusbinterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    public void run() {
                        setusbinterface(usbTargetSpinner.getSelectedItem().toString(), usbModeSpinner.getSelectedItem().toString(), usbAdbSpinner.getSelectedItem().toString());
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                nh.showMessage_long("USB interface changed");
                                refreshUSB(usbState);
                                refreshImage(imageState);
                            }
                        });
                    }
                }).start();

            }
        });

        scriptRunner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (scriptSpinner.getCount() != 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT");
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.putExtra("com.offsec.nhterm.iInitialCommand", nh.makeTermTitle("ScriptRunner") + "su -c \"sh " + script_folder_path + "/" + scriptSpinner.getSelectedItem().toString() + "\"");
                            startActivity(intent);
                        }
                    }).start();
                } else nh.showMessage_long("No shell script file is selected to run");
            }
        });

        reloadUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshUSB(usbState);
            }
        });

        reloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshImage(imageState);
            }
        });

        mountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (imageSpinner.getCount() != 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            mountImage(image_folder_path, imageSpinner.getSelectedItem().toString(), ro_checkbox.isChecked());
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    final ShellExecuter exe = new ShellExecuter();
                                    String output = exe.Executer("cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
                                    if (output.equals("")) {
                                        nh.showMessage_long("Failed to change image file, please eject on the PC or disconnect the usb first and try again.");
                                    } else {
                                        nh.showMessage_long("Image mounted");
                                    }
                                    refreshImage(imageState);
                                }
                            });
                        }
                    }).start();
                } else nh.showMessage_long("No image file is selected to mount");
            }
        });

        unmountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    public void run() {
                        UnmountImage();
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                final ShellExecuter exe = new ShellExecuter();
                                String output = exe.Executer("cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
                                if (!output.equals("")) {
                                    nh.showMessage_long("Image failed to unmount, please eject on the PC or disconnect the usb first and try again.");
                                } else {
                                    nh.showMessage_long("Image unmounted");
                                }
                                ;
                                refreshImage(imageState);
                            }
                        });
                    }
                }).start();

            }
        });
        return rootView;

    }

    private static void setusbinterface(String targets, String interfaces, String adb) {
        final ShellExecuter exe = new ShellExecuter();
        if (!interfaces.equals("reset")) {
            if (targets.equals("Windows") || targets.equals("Linux")) {
                targets = "win,";
            } else targets = "mac,";

            if (adb.equals("No adb")) adb = "";
            else adb = ",adb";
            exe.RunAsRootWithException("setprop sys.usb.config " + targets + interfaces + adb);
        } else {
            if (adb.equals("No adb")) {
                exe.RunAsRootWithException("setprop sys.usb.config mtp"); //Default state without adb is "mtp"
            } else {
                exe.RunAsRootWithException("setprop sys.usb.config adb"); //Default state with adb is "adb"
            }

        }

    }

    public void mountImage(String image_folder_path, String image_name, boolean readonly) {
        final ShellExecuter exe = new ShellExecuter();
        String command = "echo \"\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file";
        String ro = "0";
        if (readonly) ro = "1";
        if (image_name.toLowerCase().contains(".iso")) {
            command += " && echo 1 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                    " && echo " + ro + " > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        } else if (image_name.toLowerCase().contains(".img")) {
            command += " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                    " && echo " + ro + " > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        } else {
            nh.showMessage_long("Not sopported image file, please select either .iso or .img file only!!");
            return;
        }
        command += " && echo \"" + image_folder_path + "/" + image_name + "\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file";
        exe.RunAsRootOutput(command);
    }

    public void UnmountImage() {
        final ShellExecuter exe = new ShellExecuter();
        String command = "echo \"\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file" +
                " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        exe.RunAsRootOutput(command);
    }

    private void refreshUSB(final TextView usbState) {
        new Thread(new Runnable() {
            public void run() {
                usbState.post(new Runnable() {
                    @Override
                    public void run() {
                        final TextView usbState = (TextView) getView().findViewById(R.id.current_usb_state);
                        final ShellExecuter exe = new ShellExecuter();
                        final String outputUSB = exe.Executer("getprop sys.usb.state");
                        usbState.setText("Current USB state: \n" + outputUSB);
                    }
                });
            }
        }).start();

    }

    private void refreshImage(final TextView imageState) {
        new Thread(new Runnable() {
            public void run() {
                imageState.post(new Runnable() {
                    @Override
                    public void run() {
                        final TextView imageState = (TextView) getView().findViewById(R.id.mountedImage_state);
                        final ShellExecuter exe = new ShellExecuter();
                        String outputImage = exe.Executer("cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
                        if (outputImage.equals("")) outputImage = "No image is mounted.";
                        imageState.setText("Current Mounted Image: \n" + outputImage);
                    }
                });
            }
        }).start();

    }

    private void getImageFiles(final Spinner imageSpinner) {
        final String image_folder_path = nh.APP_SD_FILES_PATH + "/img_folder";
        ArrayList<String> result = new ArrayList<String>();
        File image_folder = new File(image_folder_path);
        if (!image_folder.exists()) {
            nh.showMessage_long("Creating directory for storing image files..");
            try {
                image_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        File[] filesInFolder = image_folder.listFiles();
        for (File file : filesInFolder) {
            if (!file.isDirectory()) {
                if (file.getName().contains(".img") || file.getName().contains(".iso")) {
                    result.add(file.getName());
                }
            }
        }
        ArrayAdapter<String> imageAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, result.toArray(new String[0]));
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageSpinner.setAdapter(imageAdapter);

    }

    private void getScriptFiles(final Spinner scriptSpinner) {
        final String script_folder_path = nh.APP_SD_FILES_PATH + "/punkscripts";
        ArrayList<String> result = new ArrayList<String>();
        File script_folder = new File(script_folder_path);
        if (!script_folder.exists()) {
            nh.showMessage_long("Creating directory for storing script files..");
            try {
                script_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        File[] filesInFolder = script_folder.listFiles();
        for (File file : filesInFolder) {
            if (!file.isDirectory()) {
                if (file.getName().contains(".sh")) {
                    result.add(file.getName());
                }
            }
        }
        ArrayAdapter<String> scriptAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, result.toArray(new String[0]));
        scriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scriptSpinner.setAdapter(scriptAdapter);
    }
}