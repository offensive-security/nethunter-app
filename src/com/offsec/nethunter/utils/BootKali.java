package com.offsec.nethunter.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by jmingov on 11/15/15.
 *
 * BootKali
 * *********
 *
 * This class has 2 main functions:

 **********
 * 1.- Send commands to kali:
 **********
 *
 *    - new BootKali(CMD).run() and new BootKali(CMD).run_bg()
 * EX:
 * String response = new BootKali("ls -l;uname -a;id;whoami").run()
 *        // Response is a string. :p
 * EX:
 * new BootKali("ls -l;uname -a;id;whoami").run_bg()
 *        // This has no output, is runned in the background
 *
 **********
 * 2.- Generate commands to pass to the term apk as intent (only generate)
 **********
 *
 * EX:
 *  OPEN KALI TERMINAL :: GET_KALI_SHELL_CMD()
 *
 *     String cmd = new BootKali("").GET_KALI_SHELL_CMD() // returns the cmd.
 *     // launck intent
 *
 * EX:
 *  pass comands to fenerate the terminal equivalent :: GET_KALI_SHELL_CMD()
 *
 *     String cmd = new BootKali("ls -l;uname -a;id;whoami").GET_TERM_CMD() // returns the cmd.
 *     // launck intent
 *
 *
 */
public class BootKali {
    private final String TERM_CMD;
    private final NhPaths nh = new NhPaths();
    private final String KALI_ENV;
    private final String KALI_COMMAND;

    private final String FULL_CMD;
    private final String SPACE = " ";
    private final String SINGLEQ = "'";
    private final String BOOTKALI;

    public BootKali(String cmd) {
        this.KALI_ENV = GET_KALI_ENV();
        this.KALI_COMMAND = GEN__KALI_CMD(cmd);
        this.BOOTKALI = GEN_BOOTKALI();
        this.TERM_CMD = this.BOOTKALI + GEN__KALI_TERM_CMD(cmd);
        this.FULL_CMD = this.BOOTKALI + KALI_COMMAND;
        // Log all cmd if needed
        //Log.d("BOOTKALI", this.BOOTKALI);
        //Log.d("KCOMMAND", this.KALI_COMMAND);
        //GET_KALI_DNS();
    }

    private String GET_KALI_ENV() {
        // add strings here , they will be in the kali env
        String[] ENV = {
                "USER=root",
                "SHELL=/bin/bash",
                "MAIL=/var/mail/root",
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "TERM=linux",
                "HOME=/root",
                "LOGNAME=root",
                "SHLVL=1",
                "YOU_KNOW_WHAT=THIS_IS_KALI_LINUX_NETHUNER_FROM_JAVA_BINKY"
        };
        String ENV_OUT = "";
        for (String aENV : ENV) {
            ENV_OUT = ENV_OUT + "export " + aENV + " && ";
        }
        return ENV_OUT;
    }

    //
    private String GEN_BOOTKALI() {
        return "chroot" + SPACE + nh.CHROOT_PATH + SPACE;
    }

    private String GEN__KALI_CMD(String cmd) {
        // return "/bin/bash -c" + SPACE + SINGLEQ + KALI_ENV + CLEAR_TERM + cmd + SINGLEQ;
        return "/bin/bash -c" + SPACE + SINGLEQ + KALI_ENV + cmd + SINGLEQ;
    }

    private String GEN__KALI_TERM_CMD(String cmd) {
        // return "/bin/bash -c" + SPACE + SINGLEQ + KALI_ENV + CLEAR_TERM + cmd + SINGLEQ;
        String CLEAR_TERM = "clear && ";
        return "/bin/bash -c" + SPACE + SINGLEQ + KALI_ENV + CLEAR_TERM + cmd + SINGLEQ;
    }

    // is really needed?????
    private Boolean GET_ANDROID_DNS() {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method method = SystemProperties.getMethod("get", String.class);
            ArrayList<String> servers = new ArrayList<>();
            //String dns_servers = "";
            for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
                String value = (String) method.invoke(null, name);
                if (value != null && !"".equals(value) && !servers.contains(value)) {
                    servers.add(value);
                    Log.d("DNS:", value);
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    *
    * PUBLIC METHODS
    *
     */


    // blocking with output
    // sends a command to kali
    public String run() {
        String output = "";
        String line;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((BOOTKALI + KALI_COMMAND).getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line + "\n";
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell out:", output);
                Log.e("Shell Error:", line);
            }
            br.close();
            process.waitFor();
            process.destroy();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    // sends a command to kali
    // no blocking but atm no output
    public void run_bg() {
        new Thread(() -> {
            String output = "";
            String line;
            try {
                Process process = Runtime.getRuntime().exec("su");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                //InputStream stdout = process.getInputStream();
                stdin.write((BOOTKALI + KALI_COMMAND).getBytes());
                stdin.flush();
                stdin.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    Log.e("Shell out:", output);
                    Log.e("Shell Error:", line);
                }
                br.close();
                process.waitFor();
                process.destroy();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // these don't seem to be used
    public String GET_TERM_CMD() {
        return "su -c \"" + TERM_CMD + "\"";
    }

    // this string is the comand to pop a kaly shell (intent to the terminal, pass this a command)
    public String GET_KALI_SHELL_CMD() {
        return "su -c \"clear && " + BOOTKALI + "/bin/login -f root \"";
    }

    public String GET_CMD() {
        return FULL_CMD;
    }


}
