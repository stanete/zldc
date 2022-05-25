package com.example.android.zldc.brr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class BR_UpdateApp extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            try{
                Uri data = intent.getData();
                if (data != null && context.getPackageName().equals(data.getEncodedSchemeSpecificPart())) {
                    Log.d("TAG", "zldc重新启动.....");
                    // 重新启动APP
                    Intent intentToStart = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    context.startActivity(intentToStart);
                }
            } catch (Exception e) {

            }
        }
    }
}
