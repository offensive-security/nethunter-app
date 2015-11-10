/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

public class CustomCommand {

    private long id;
    private String btn_label;
    private String command;
    private String execMode;
    private String sendToShell;

    final static String TABLE = "LAUNCHERS";
    final static String ID = "ID";
    final static String BTN_LABEL = "BTN_LABEL";
    final static String CMD = "COMMAND";
    final static String EXEC_MODE = "EXEC_MODE";
    final static String SEND_TO_SHELL = "SEND_TO_SHELL";

    public CustomCommand(){

    }
    public CustomCommand(long id, String btn_label, String command, String execMode, String sendToShell) {
        this.id = id;
        this.btn_label = btn_label;
        this.command = command;
        this.execMode = execMode;
        this.sendToShell = sendToShell;
    }

    public void setId(long id) { this.id = id; }

    public long getId() {
        return id;
    }

    public String getCommand_label() {
        return btn_label;
    }

    public void setBtn_label(String btn_label) {
        this.btn_label = btn_label;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getExec_Mode() {
        return execMode;
    }
    public void setExec_Mode(String execMode) {
        this.execMode = execMode;
    }

    public String getSend_To_Shell() {
        return sendToShell;
    }
    public void setSend_To_Shell(String sendToShell) {
        this.sendToShell = sendToShell;
    }
}