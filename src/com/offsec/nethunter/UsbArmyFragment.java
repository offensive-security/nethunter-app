package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;


public class UsbArmyFragment extends Fragment {

    private SharedPreferences sharedpreferences;
    private static NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final ShellExecuter exe = new ShellExecuter();
    private Context mContext;
    private static final String image_folder_path = "/sdcard/nh_files/img_folder";
    private static final String script_folder_path = "/sdcard/nh_files/punkscripts";
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
        nh = new NhPaths();
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        View rootView = inflater.inflate(R.layout.usbarmy, container, false);
        mContext = getActivity().getApplicationContext();
        //final Button button = (Button) rootView.findViewById(R.id.updateOptions);
        //button.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        updateOptions();
        //    }
        //});
        //setHasOptionsMenu(true);
        getUSBInterfaces(rootView);

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
        int current_usb_target = -1;
        int current_usb_mode = -1;
        int current_usb_adb = -1;
        String image_file[] = getImageFiles();
        String script_file[] = getScriptFiles();
        for (String cc : getResources().getStringArray(R.array.usb_targets)) {
            current_usb_target++;
            if (cc.equals(sharedpreferences.getString("usb_targets", cc))) {
                break;
            }
        }

        for (String cc : getResources().getStringArray(R.array.usb_states_win_lin)) {
            current_usb_mode++;
            if (cc.equals(sharedpreferences.getString("usb_states", cc))) {
                break;
            }
        }

        for (String cc : getResources().getStringArray(R.array.usb_adb)) {
            current_usb_adb++;
            if (cc.equals(sharedpreferences.getString("usb_adb", cc))) {
                break;
            }
        }

        ArrayAdapter<String> imageAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, image_file);
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageSpinner.setAdapter(imageAdapter);

        ArrayAdapter<String> scriptAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, script_file);
        scriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scriptSpinner.setAdapter(scriptAdapter);

        usbTargetSpinner.setSelection(current_usb_target);
        usbModeSpinner.setSelection(current_usb_mode);
        usbAdbSpinner.setSelection(current_usb_adb);
        imageSpinner.setSelection(0);
        scriptSpinner.setSelection(0);

        usbTargetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // update sharedpreferences
                String items = usbTargetSpinner.getSelectedItem().toString();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("usb_targets", items);
                editor.apply();
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

        usbModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // update sharedpreferences
                String items = usbModeSpinner.getSelectedItem().toString();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("usb_states", items);
                editor.apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });

        usbAdbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // update sharedpreferences
                String items = usbAdbSpinner.getSelectedItem().toString();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("usb_adb", items);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });


        imageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        scriptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItemText = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
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
                                nh.showMessage("USB interface changed");
                                refreshUSB();
                            }
                        });
                    }
                }).start();

            }
        });

        scriptRunner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    public void run() {

                        Intent intent = new Intent("com.offsec.nhterm.RUN_SCRIPT");
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.putExtra("com.offsec.nhterm.iInitialCommand", nh.makeTermTitle("ScriptRunner") + "su -c \"sh " + script_folder_path + scriptSpinner.getSelectedItem().toString() + "\"");
                        startActivity(intent);
                        //CustomCommand runscriptComand = new CustomCommand(1, "scriptRunner", scriptSpinner.getSelectedItem().toString(), "INTERACTIVE", "ANDROID", 0);
                        //    new BootKali(runscriptComand.getCommand()).run();
                        //    nh.showMessage("Android cmd done.");
                    }
                }).start();

            }
        });



        reloadUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshUSB();
            }
        });

        reloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshImage();
            }
        });

        mountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    public void run() {
                        mountImage(image_folder_path, imageSpinner.getSelectedItem().toString());
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                nh.showMessage("Image mounted");
                                refreshImage();
                            }
                        });
                    }
                }).start();

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
                                    nh.showMessage("Image failed to unmount, please eject on the PC or disconnect the usb first and try again.");
                                } else {
                                    nh.showMessage("Image unmounted");
                                };
                                refreshImage();
                            }
                        });
                    }
                }).start();

            }
        });
        //Simon end
        return rootView;

    }

    private void getUSBInterfaces(final View rootView) {

        nh = new NhPaths();
        final TextView usbState = (TextView) rootView.findViewById(R.id.current_usb_state);
        final TextView imageState = (TextView) rootView.findViewById(R.id.mountedImage_state);
        new Thread(new Runnable() {
            final ShellExecuter exe = new ShellExecuter();
            final String outputUSB = exe.Executer("getprop sys.usb.state");
            String outputImage = exe.Executer("cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
            public void run() {
                usbState.post(new Runnable() {
                    @Override
                    public void run() {
                        usbState.setText("Current USB state: \n" + outputUSB);
                        if (outputImage.equals("")) outputImage = "No image is mounted.";
                        imageState.setText("Current Mounted Image: \n" + outputImage);
                    }
                });
            }
        }).start();
    }
    private static void setusbinterface(String targets, String interfaces, String adb) {
        final ShellExecuter exe = new ShellExecuter();
        if (!interfaces.equals("reset")) {
            if (targets.equals("Windows") || targets.equals("Linux")) {
                targets = "win,";
            } else targets = "mac,";

            if (adb.equals("No adb")) adb = ""; else adb = ",adb";
            exe.RunAsRootWithException("setprop sys.usb.config " + targets + interfaces + adb);
        } else {
            if (adb.equals("No adb")) {
                exe.RunAsRootWithException("setprop sys.usb.config reset");
            } else {
                exe.RunAsRootWithException("setprop sys.usb.config reset,adb");
            }

        }

    }

    private static void mountImage(String script_path, String script_name) {
        final ShellExecuter exe = new ShellExecuter();
        String command = "echo \"\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file";
        if (script_path.contains(".iso")) {
            command += " && echo 1 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                       " && echo 1 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        } else {
            command += " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                       " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        }
        command += " && echo \"" + script_path + "/" + script_name + "\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file";
        exe.RunAsRootOutput(command);

    }

    private static void UnmountImage() {
        final ShellExecuter exe = new ShellExecuter();
        String command = "echo \"\" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file" +
                  " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom" +
                  " && echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro";
        exe.RunAsRootOutput(command);
    }

    private void refreshUSB() {
        if (getView() == null) {
            return;
        }
        final TextView usbState = (TextView) getView().findViewById(R.id.current_usb_state);
        final ShellExecuter exe = new ShellExecuter();
        final String outputUSB = exe.Executer("getprop sys.usb.state");
        usbState.setText("Current USB state: \n" + outputUSB);
    }

    private void refreshImage() {
        if (getView() == null) {
            return;
        }
        final TextView imageState = (TextView) getView().findViewById(R.id.mountedImage_state);
        final ShellExecuter exe = new ShellExecuter();
        String outputImage = exe.Executer("cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file");
        if (outputImage.equals("")) outputImage = "No image is mounted.";
        imageState.setText("Current Mounted Image: \n" + outputImage);
    }

    private String[] getImageFiles() {
        ArrayList<String> result = new ArrayList<String>();
        File image_folder = new File(image_folder_path);
        if (!image_folder.exists()){
            nh.showMessage_long("Creating directory for storing script files..");
            try {
                image_folder.mkdir();
            } catch (Exception e){
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
        return result.toArray(new String[0]);
    }

    private String[] getScriptFiles() {
        ArrayList<String> result = new ArrayList<String>();
        File script_folder = new File(script_folder_path);
        if (!script_folder.exists()) {
            nh.showMessage_long("Creating directory for storing script files..");
            try {
                script_folder.mkdir();
            } catch (Exception e){
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
        return result.toArray(new String[0]);
    }

    private String getDeviceName() {
        return Build.DEVICE;
    }

    public Boolean isOPO5() {
        return getDeviceName().equalsIgnoreCase("A5000") ||
                getDeviceName().equalsIgnoreCase("A5010") ||
                getDeviceName().equalsIgnoreCase("OnePlus5") ||
                getDeviceName().equalsIgnoreCase("OnePlus5T");
    }
    //Simon end
}
