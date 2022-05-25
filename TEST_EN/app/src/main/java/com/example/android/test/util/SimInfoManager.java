package com.example.android.test.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Sim卡信息管理
 * <p>
 * <!-- 添加访问手机位置的权限 -->
 * <p>
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 * <p>
 * <!-- 添加访问手机状态的权限 -->
 * <p>
 * <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 */
public class SimInfoManager {
    public static TelephonyManager getTelephonyManager(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceid = tm.getDeviceId();//获取智能设备唯一编号
//        String te1  = tm.getLine1Number();//获取本机号码
//        String imei = tm.getSimSerialNumber();//获得SIM卡的序号
//        String imsi = tm.getSubscriberId();//得到用户Id
        return tm;
    }
}
