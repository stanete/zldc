package com.example.android.test;

import android.app.Application;
import android.os.Environment;

public class MyApp extends Application {
    public static boolean isLocked=true;
    public static String password;
    @Override
    public void onCreate() {
        super.onCreate();
        Config.context = this;
        Config.fPath=getExternalFilesDir(null).getPath();
        Config.apkFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/apk";
        Config.mainFpath= Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/main";
        Config.subFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/sub";
        Config.readConfig();
        password="voltz_zld_123";
    }
}