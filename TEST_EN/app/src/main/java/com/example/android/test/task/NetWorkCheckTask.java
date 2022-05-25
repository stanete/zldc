package com.example.android.test.task;

import android.content.Context;
import android.util.Log;

import com.example.android.test.MyLog;
import com.example.android.test.util.NetworkUtil;

import java.util.TimerTask;

public class NetWorkCheckTask extends TimerTask {
    private MyLog myLog=MyLog.getInstance();
    private Context context;
    /**
     * 默认为-1，没有网络
     * 0 移动
     * 1 wifi
     */
    private int netWorkType=0;
    @Override
    public void run() {
        netWorkType = NetworkUtil.getNetWorkStates(context);
//        myLog.Write_Log(MyLog.LOG_INFO,"检测网络状态："+netWorkType);
        Log.e("netWorkType",netWorkType+"");
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getNetWorkType() {
        return netWorkType;
    }

    public void setNetWorkType(int netWorkType) {
        this.netWorkType = netWorkType;
    }
}
