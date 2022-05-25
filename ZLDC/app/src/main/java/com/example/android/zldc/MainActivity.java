package com.example.android.zldc;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.zldc.sers.MyService;

import java.util.List;

public class MainActivity extends Activity {
    private MyLog myLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopService(new Intent(MainActivity.this, MyService.class));
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
        Toast.makeText(MainActivity.this,"service is starting...",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        finish();
        super.onResume();
    }
}