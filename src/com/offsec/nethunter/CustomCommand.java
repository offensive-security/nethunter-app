/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

public class CustomCommand {

    private long id;
    private String command_label;
    private String command;
    private String execMode;
    private String sendToShell;
    private Integer runAtBoot;

    final static String TABLE = "LAUNCHERS";
    final static String ID = "ID";
    final static String BTN_LABEL = "BTN_LABEL";
    final static String CMD = "COMMAND";
    final static String EXEC_MODE = "EXEC_MODE";
    final static String SEND_TO_SHELL = "SEND_TO_SHELL";
    final static String RUN_AT_BOOT = "RUN_AT_BOOT";
    CustomCommand(){

    }
    CustomCommand(long id, String command_label, String command, String execMode, String sendToShell, Integer runAtBoot) {
        this.id = id;
        this.command_label = command_label;
        this.command = command;
        this.execMode = execMode;
        this.sendToShell = sendToShell;
        this.runAtBoot = runAtBoot;

    }

    public void setId(long id) { this.id = id; }

    public long getId() {
        return id;
    }

    String getCommand_label() {
        return command_label;
    }

    void setCommand_label(String command_label) {
        this.command_label = command_label;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    String getExec_Mode() {
        return execMode;
    }
    void setExec_Mode(String execMode) {
        this.execMode = execMode;
    }

    String getSend_To_Shell() {
        return sendToShell;
    }
    void setSend_To_Shell(String sendToShell) {
        this.sendToShell = sendToShell;
    }

    Integer getRun_At_Boot() {
        return runAtBoot;
    }
    void setRun_At_Boot(Integer runAtBoot) {
        this.runAtBoot = runAtBoot;
    }
}