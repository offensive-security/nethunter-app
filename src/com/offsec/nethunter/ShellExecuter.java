package com.offsec.nethunter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ShellExecuter {

    final static String TAG = "ShellExecutor";

    public ShellExecuter() {

    }

    public String Executer(String command) {
        StringBuilder output = (new StringBuilder());
        try {
            List<String> shellOut = Shell.SU.run(command);
            for (String line : shellOut) {
                output.append(line).append((char)10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public String Executer(String command[]) {
        StringBuilder output = (new StringBuilder());
        try {
            List<String> shellOut = Shell.SU.run(command);
            for (String line : shellOut) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public void RunAsRoot(String[] command) {
        try {
            Shell.SU.run(command);
        } catch (Exception e) {
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

    public String RunAsRootOutput(String command) {
        StringBuilder output = (new StringBuilder());
        try {
            List<String> shellOut = Shell.SU.run(command);
            for (String line : shellOut) {
                output.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
