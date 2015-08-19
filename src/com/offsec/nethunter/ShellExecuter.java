package com.offsec.nethunter;

import android.util.Log;
import java.util.List;
import eu.chainfire.libsuperuser.Shell;

public class ShellExecuter {

    final static String TAG = "ShellExecutor";
    final static boolean LOGGING = true;
    public ShellExecuter() {

    }

    // Each method MUST accept the params as string or string[]
    // the prefix "su -c" or "sh -c insnt longer needed in the commands"


    // M.1
    // Executer() => Just exec the command/s and get output
    //            => OUTPUT:true (String whit separators -> "\n" )
    public String Executer(String command) {
        if(LOGGING){
            Log.d(TAG, "Executer ::: " + command);
        }
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
    // M.2
    // RunAsRoot() => Just exec the command/s
    //             => OUTPUT:false
    public void RunAsRoot(String command) {
        if(LOGGING){
            Log.d(TAG, "RunAsRoot ::: " + command);
        }
        try {
           Shell.SU.run(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void RunAsRoot(String[] command) {
        try {
            Shell.SU.run(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // M.3
    // RunAsRootOutput() => Just exec the command/s and get output
    //                   => OUTPUT:true (String without separators)
    public String RunAsRootOutput(String command) {
        if(LOGGING){
            Log.d(TAG, "RunAsRootOutput ::: " + command);
        }
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
    public String RunAsRootOutput(String[] command) {
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
