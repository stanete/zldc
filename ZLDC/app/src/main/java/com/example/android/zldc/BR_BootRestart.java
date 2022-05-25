package com.example.android.zldc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BR_BootRestart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            try {
                //4G模块更新后需要等待网络初始化，否则会被系统KILL
//                Thread.sleep(10 * 1000);
                Intent startIntent = new Intent(context, MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
            } catch (Exception e) {
            }
        }
    }
}
