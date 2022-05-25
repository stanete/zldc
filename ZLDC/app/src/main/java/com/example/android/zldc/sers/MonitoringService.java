package com.example.android.zldc.sers;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;
import com.example.android.zldc.MainActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 检测应用是否存活
 */
public class MonitoringService extends Service {
    private final static String TAG = "MonitoringService";
    public static final String CANCEL_MONITOR = "cancelMonitor";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CANCEL_MONITOR.equals(intent.getAction())) {
                Log.e(TAG, "onReceive:kill app process！");
                killMyselfPid(); // 杀死自己的进程
            }
        }
    };

    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            checkIsAlive();
        }
    };

    /**
     * 检测应用是否活着
     */
    private void checkIsAlive() {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA).format(new Date());
        Log.e(TAG, "CustodyService Run: " + format);
        String pName = "com.example.android.zldc";
        int uid = getPackageUid(getApplicationContext(), pName);
        if (uid > 0) {
            //程序是否存活
//            boolean rstA = isAppRunning(getApplicationContext(), pName);
            boolean rstA = isServiceRunning(getApplicationContext(),"com.example.android.zldc.sers.MyService");
            //进程是否存活
            boolean rstB = isProcessRunning(getApplicationContext(), uid);
            Log.e(TAG, "rstA:" + rstA + ", rstB:" + rstB);
            if (rstA) {
                //指定包名的程序正在运行中
                Log.d(TAG, "service is running...");
                isTopRunning(getApplicationContext(), pName);
            } else {
                //指定包名的程序未在运行中
                Log.d(TAG, "App is not running, restart app...");
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MonitoringService.this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            //应用未安装
            Log.e(TAG, "App is not installed.");
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: Start monitor! ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CANCEL_MONITOR);
        registerReceiver(broadcastReceiver, intentFilter);
        timer.schedule(task, 0, 10*1000l);// 设置检测的时间周期(毫秒数)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 杀死自身的进程
     */
    private void killMyselfPid() {
        int pid = android.os.Process.myPid();
        String command = "kill -9 " + pid;
        Log.e(TAG, "killMyselfPid: " + command);
        stopService(new Intent(MonitoringService.this, MonitoringService.class));
        try {
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     * Created by cafeting on 2017/2/4.
     *
     * @param context     上下文
     * @param packageName 应用的包名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param context     上下文
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    //获取已安装应用的 uid，-1 表示未安装此应用或程序异常
    public static int getPackageUid(Context context, String packageName) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                Log.d(TAG, "app uid:" + applicationInfo.uid);
                return applicationInfo.uid;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    /**
     * 判断某一 uid 的程序是否有正在运行的进程，即是否存活
     * Created by cafeting on 2017/2/4.
     *
     * @param context 上下文
     * @param uid     已安装应用的 uid
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isProcessRunning(Context context, int uid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() > 0) {
            for (ActivityManager.RunningServiceInfo appProcess : runningServiceInfos) {
                if (uid == appProcess.uid) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void isTopRunning(Context context, String pName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = am.getRunningTasks(1).get(0).topActivity;
        Log.d("Utils", "top activity is "+componentName.getPackageName());
        String packageName = componentName.getPackageName();
        if(packageName.equals(pName)){
            return;
        }

        List<ActivityManager.RunningTaskInfo> runningTaskInfos = am.getRunningTasks(10);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (context.getPackageName().equals(runningTaskInfo.topActivity.getPackageName())) {
                am.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }
}
