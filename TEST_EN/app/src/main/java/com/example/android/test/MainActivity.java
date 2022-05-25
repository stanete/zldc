package com.example.android.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.example.android.test.sers.MyService;

public class MainActivity extends Activity {
    private MyLog myLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopService(new Intent(this, MyService.class));
        String logPath = getExternalFilesDir(null).getPath() + "/log";
        if (myLog == null) {
            myLog = MyLog.getInstance();
            myLog.Init(logPath);
        }
        Config.context = this;
        Config.fPath = getExternalFilesDir(null).getPath();
        Config.apkFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/apk";
        Config.mainFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/main";
        Config.subFpath = Config.fPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/sub";
        Config.readConfig();
        Toast.makeText(this,"service is starting...",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        finish();
        super.onResume();
    }
}