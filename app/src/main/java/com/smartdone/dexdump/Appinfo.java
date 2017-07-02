package com.smartdone.dexdump;

/**
 * Created by smartdone on 2017/7/2.
 */

public class Appinfo {
    private String appName;
    private String appPackage;
    private boolean isChecked;

    public Appinfo() {

    }

    public Appinfo(String appName, String appPackage) {
        this.appName = appName;
        this.appPackage = appPackage;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

}
