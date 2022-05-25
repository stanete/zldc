package com.example.android.zldc;

import android.app.Application;
import android.os.Environment;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config.context = this;
        Config.fPath=getExternalFilesDir(null).getPath();
        Config.apkFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/apk";
        Config.mainFpath= Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/main";
        Config.subFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/sub";
        Config.readConfig();
    }
}