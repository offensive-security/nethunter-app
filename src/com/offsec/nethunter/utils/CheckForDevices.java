package com.offsec.nethunter.utils;

import android.os.Build;

public class CheckForDevices {
    private String DeviceName;

    public CheckForDevices() {
        DeviceName = Build.DEVICE;
    }

    public Boolean isOPO() {
        return DeviceName.equalsIgnoreCase("bacon") ||
                DeviceName.equalsIgnoreCase("A0001") ||
                DeviceName.equalsIgnoreCase("one") ||
                DeviceName.equalsIgnoreCase("OnePlus");
    }

    public Boolean isOPO2() {
        return DeviceName.equalsIgnoreCase("A2001") ||
                DeviceName.equalsIgnoreCase("A2003") ||
                DeviceName.equalsIgnoreCase("A2005") ||
                DeviceName.equalsIgnoreCase("OnePlus2");
    }

    public Boolean isOPO5() {
        return DeviceName.equalsIgnoreCase("A5000") ||
                DeviceName.equalsIgnoreCase("A5010") ||
                DeviceName.equalsIgnoreCase("OnePlus5") ||
                DeviceName.equalsIgnoreCase("OnePlus5T");
    }

    public Boolean isOldDevice() {
        return DeviceName.equalsIgnoreCase("flo") ||
                DeviceName.equalsIgnoreCase("deb") ||
                DeviceName.equalsIgnoreCase("mako");
    }
}
