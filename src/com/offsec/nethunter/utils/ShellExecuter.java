package com.offsec.nethunter.utils;

import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class ShellExecuter {

    private final static String TAG = "ShellExecutor";

    public ShellExecuter() {

    }

    public String Executer(String command) {
        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public String Executer(String command[]) {
        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public void RunAsRoot(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String tmpmd : command) {
                os.writeBytes(tmpmd + '\n');
            }
            os.writeBytes("exit\n");
            os.flush();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String RunAsRootWithException(String command) throws RuntimeException {
        try {
            String output = "";
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line;
            }
            br.close();

            // Lint says while does not loop here (probably because it doesn't do anything except shell error)
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell Error:", line);
                throw new RuntimeException();
            }
            br.close();

            process.waitFor();
            process.destroy();
            return output;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    boolean isRootAvailable() {

        NhPaths nh;
        nh = new NhPaths();

        String busybox_ver = nh.whichBusybox() + " id -u";
        String result = RunAsRootOutput(busybox_ver);

        return result.equals("0");
    }

    public String RunAsRootOutput(String command) {
        String output = "";
        String line;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line;
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell Error:", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        return output;
    }

    // this method accepts a text viu (prefect for cases like mana fragment)
    // if you need to manipulate the outpput use the SYNC method. (down)
    public void ReadFile_ASYNC(String _path, final EditText v) {
        final String command = "cat " + _path;
        new Thread(() -> {
            String output = "";
            try {
                Process  p = Runtime.getRuntime().exec("su -c " + command);
                p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output = output +  line + "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String _output = output;
            v.post(() -> v.setText(_output));
        }).start();
    }
    // WRAP THIS IN THE BACKGROUND IF POSIBLE WHE USING IT
    public String ReadFile_SYNC(String _path) {
        StringBuilder output = new StringBuilder();
        String command = "cat " + _path;
        Process p;
        try {
            p = Runtime.getRuntime().exec("su -c " + command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
    // SAVE FILE CONTENTS: (contents, fullFilePath)
    public boolean SaveFileContents(String contents, String _path){

        String _newCmd = "cat << 'EOF' > "+_path+"\n"+contents+"\nEOF";
        String _res = RunAsRootOutput(_newCmd);
        if(_res.equals("")){ // no error we fine
            return true;
        } else {
            Log.d("ErrorSavingFile: ", "Error: " + _res);
            return false;
        }
    }
}
