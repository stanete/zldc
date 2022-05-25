package com.example.android.test.task;
import android.content.Context;
import android.util.Log;

import com.example.android.test.Config;
import com.example.android.test.LocalData;
import com.example.android.test.util.WifiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.TimerTask;

/**
 * 网络连接任务
 */
public class NetWorkConnectTask extends TimerTask {
    private Context context;
    private WifiUtils wifiUtils;
    @Override
    public void run() {
        Log.e("networking","网络连接任务开始");
        wifiUtils=WifiUtils.getInstance(context);
        if(LocalData.isNetworking){
            Log.e("networking","已连接");
           return;
        }
        try{
            wifiUtils.closeWifi();
            Thread.sleep(2000l);
            if(!wifiUtils.isWifiEnable()) {
                wifiUtils.openWifi();
                Thread.sleep(5000l);
            }
            String wifiName=Config.wifiName;
            String wifiPass=Config.wifiPass;
            Log.i("networking","正在连接wifi:"+wifiName+"\twifi密码:"+wifiPass);
            if(StringUtils.isBlank(wifiName)){
                return;
            }
            Thread.sleep(4000l);
            wifiUtils.connectWifiPws(wifiName,wifiPass);
        }catch(Exception e){
            Log.e("networking","wifi连接异常:"+e.toString());
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
