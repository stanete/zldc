package com.example.android.test.sers;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.example.android.test.Api;
import com.example.android.test.CommonClass;
import com.example.android.test.Config;
import com.example.android.test.LocalData;
import com.example.android.test.MainActivity;
import com.example.android.test.MqttManager;
import com.example.android.test.MyLog;
import com.example.android.test.PackageUtils;
import com.example.android.test.SeriaPort;
import com.example.android.test.ShellUtils;
import com.example.android.test.UpdateData;
import com.example.android.test.bean.Coordinates;
import com.example.android.test.bean.MainConfig;
import com.example.android.test.db.DBManager;
import com.example.android.test.eventbus.EventBusUtil;
import com.example.android.test.interfaces.OnMqttAndroidConnectListener;
import com.example.android.test.task.AlertsDataTask;
import com.example.android.test.task.AlertsUpTask;
import com.example.android.test.task.BatteryInfoTask;
import com.example.android.test.task.CheckTimeoutTask;
import com.example.android.test.task.LockerInfoTask;
import com.example.android.test.task.LogSenderTask;
import com.example.android.test.task.NetWorkCheckTask;
import com.example.android.test.toservers.ToUpdateServer;
import com.example.android.test.util.GPSUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyService extends Service {

    private static boolean isConnected = false;
    /**
     * wifi
     */
    private static final int NETWORKTYPE_WIFI = 0;
    /**
     * 4g??????
     */
    private static final int NETWORKTYPE_4G = 1;
    /**
     * 2g??????
     */
    private static final int NETWORKTYPE_2G = 2;
    /**
     * ??????????????????
     */
    private static final int NETWORKTYPE_NONE = 3;
    /**
     * mqtt????????????????????????????????????
     */
    private static boolean isFirstConnect = true;
    public static Thread updateThread;  //????????????
    /**
     * ?????????????????????
     */
    private boolean isExchange = false;
    /**
     * ?????????????????????
     */
    private boolean isUpdating = false;
    /**
     * ?????????
     */
    private Coordinates coordinates;
    /**
     * ????????????
     */
    private static final String EXIST_FLAG = "exist";
    /**
     * ?????????????????????????????????????????????app
     */
    public static boolean isNet = false;
    //??????????????????????????????????????????????????????????????????
    UpdateData hostUpdateData;//???????????????????????????
    UpdateData netUpdateData;//???????????????????????????
    ToUpdateServer toUpdateServer;//OTA???????????????
    //??????
    private static int[] counts = new int[3];
    /**
     * ?????????????????????
     */
    public TelephonyManager mTelephonyManager;
    /**
     * ?????????????????????
     */
    public PhoneStatListener mListener;
    /**
     * ???????????????????????????
     */
    private final boolean isFinish = true;
    SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    private Dialog dialog;
    private ImageView imageView;
    private SeriaPort seriaPort;
    private TextView tex_time;
    private static Camera mCamera;
    private static final String DB_NAME = "mapping.db";
    private static final int DB_VERSION = 1;
    private static DBManager dbManager;
    private final static Timer timer = new Timer();
    private BatteryInfoTask batteryInfoTask;
    private LockerInfoTask lockerInfoTask;
    public static RebootDeviceTask rebootDeviceTask;
    public static NetWorkCheckTask nwct;
    private LogSenderTask logSenderTask;
    private AlertsDataTask adt;
    private AlertsUpTask aut;
    String upfilemain, upfileapp, upfilesub_file;
    EventBusUtil.EventData eventData = new EventBusUtil.EventData();
    public static Thread checkThread;
    private static CheckTimeoutTask ctt;
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(6);
    private final ArrayList<View> views = new ArrayList<>();
    SimpleDateFormat dfsf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    SimpleDateFormat dfsc = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    public static final Timer timeoutTimer=new Timer();
    MyLog myLog;

    private Location location;

    private Handler handler = new Handler();

    private GPSUtils gpsUtils;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            location = gpsUtils.getLocation();//??????????????????
            if (location != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(1000l);
                                setCoordinates(location);
                            } catch (Exception e) {

                            }
                        }
                    }
                }).start();
                handler.removeCallbacks(runnable);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    OnMqttAndroidConnectListener mqttAndroidConnectListener = new OnMqttAndroidConnectListener() {
        @Override
        public void onDataReceive(final String topic, final String message) {
            Log.e("???????????????,????????? ", topic + " & " + message);
            myLog.Write_Log(MyLog.LOG_ERROR, "???????????????" + topic + "\n???????????????" + message);
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Log.v("linkany_sub_thread", "?????????" + threadName + "?????????????????????" + topic);
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        if (topic.equals(Api.Topic_R_OpenBox)) {
                            //???????????????soc?????????
                            LocalData.getBatSocInPreAndAfter().setPer(0);
                            if (StringUtils.isNotBlank(message)) {
                                Api.BoxOpenComm boxComm = Api.getBoxOpenComm();
                                boxComm.setAllData(jsonObject);
                                int slotId = boxComm.getSlotId();
                                LocalData.Bms bms = LocalData.getBatData(String.valueOf(slotId));
                                if (bms != null) {
                                    int soc = bms.getBmsData().getSoc();
                                    LocalData.getBatSocInPreAndAfter().setPer(soc);
                                }
                                int slotInfo = boxComm.getSlotInfo();
                                //??????????????????
                                seriaPort.Command_OpenBox(slotId, slotInfo);
                            } else {
                                //?????????????????????
                                Log.e("message", "no message body");
                            }
                        }
                        if(topic.equals(Api.Topic_R_Thresholds)){
                            MainConfig mc= JSON.parseObject(message,MainConfig.class);
                            MainConfig.setConfig(mc);
                            myLog.Write_Log(MyLog.LOG_INFO, "????????????" + message + "???");
                            seriaPort.writeConfig(mc);
                            Thread.sleep(4000l);
                            //????????????
                            int pid = android.os.Process.myPid();
                            String command = "kill -9 " + pid;
                            myLog.Write_Log(MyLog.LOG_INFO, "killMyselfPid: " + command);
                            stopService(new Intent(getApplicationContext(), MonitoringService.class));
                            try {
                                Runtime.getRuntime().exec(command);
                                System.exit(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //????????????
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e("open_box_err", e.toString());
                    }
//                        Log.e("linkany_local_order_data",telManager.getSubscriberId());
                }
            });
        }
    };


    public MyService() {
        isConnected = false;
        Log.e("MyService", "MyService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind", "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String logPath = getExternalFilesDir(null).getPath() + "/log";
        if (myLog == null) {
            myLog = MyLog.getInstance();
            myLog.Init(logPath);
        }
        adt=new AlertsDataTask();
        aut=new AlertsUpTask();
        ctt=new CheckTimeoutTask();
        logSenderTask = new LogSenderTask();
        logSenderTask.setLogPath(logPath);
        coordinates = new Coordinates();
        toUpdateServer = new ToUpdateServer();
        Config.mac = getMac(this);
        hostUpdateData = new UpdateData();
        netUpdateData = new UpdateData();
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000l);//?????????????????????
                    String s = "??????????????????????????????????????? " + (Config.zldConfigPeriod * 5) + "???/???";
                    myLog.Write_Log(MyLog.LOG_INFO, s);
                    Log.e("configPeriod", s);
                    if (!Config.apkFname.isEmpty()) {
                        //???????????????????????????
                        String upfile = hostUpdateData.getApk().getFilename();
                        //??????????????????????????????
                        if (!Config.apkFname.equals(upfile)) {
                            try {
                                toUpdateServer.PostUploadCommon("Apk", upfile, Config.apkFname, true, true, -1, "");
                                Map map = Config.getConfig_map();
                                map.put("apkFname", upfile);
                                Config.Set_Config(map);
                                //????????????
                                LocalData.Updating u=LocalData.getUpdating();
                                u.setAppUpdating(false);
                                LocalData.setUpdating(u);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????????????????" + e.toString());
                }
                while (true) {
                    try {
                        long start = System.currentTimeMillis();
                        getZldConfigData();
                        int sleept = (int) ((5 * 1000 * Config.zldConfigPeriod)
                                - (System.currentTimeMillis() - start));
                        if (sleept > 0)
                            Thread.sleep(sleept);
                        toUpdateServer.GetDeviceID();

                    } catch (Exception e) {
                        myLog.Write_Log(MyLog.LOG_INFO, "Exception Update_Thread " + e.toString());
                    }
                }
            }
        });

        batteryInfoTask = new BatteryInfoTask();
        lockerInfoTask = new LockerInfoTask();

        nwct = new NetWorkCheckTask();
        nwct.setContext(getApplicationContext());
        //??????????????????
        timer.schedule(nwct, 0l, 1000l);
        //??????30???????????????
        timer.schedule(adt,30000l,1000l);
        try {
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            String[] strings = packageInfo.versionName.split("\\.");
            String apkVersion = "V" + strings[0] + strings[1] + packageInfo.versionCode;
            Config.mainVersion = Integer.valueOf(strings[0]);
            Config.subVersion = Integer.valueOf(strings[1]);
            Config.resvision = Integer.valueOf(packageInfo.versionCode);
        } catch (Exception e) {
            myLog.Write_Log(MyLog.LOG_ERROR, "???????????????????????????" + e.toString());
            Log.e("exception", "????????????????????????:" + e.toString());
        }
        //TODO ?????????????????????
        hostUpdateData.setApk(new UpdateData.Apk("VOLTZ_ZLD_AH200_P102_S"+ Config.mainVersion + Config.subVersion + Config.resvision + ".apk",
                Config.mainVersion, Config.subVersion, Config.resvision));
        try {
            Api.getInitData().SetAllData();
        } catch (Exception e) {

        }
        rebootDeviceTask = new RebootDeviceTask();
        //?????????????????????????????????
        try {
            Init();
        } catch (Exception e) {
        }
        boolean isConnected = MqttManager.getInstance().isConnected();
        Log.e("isConnected", "????????????????????????" + isConnected);

        return super.onStartCommand(intent, flags, startId);
    }

    //????????????????????????
    private void Init() throws Exception {
//        gpsUtils = new GPSUtils(MainActivity.this);//?????????GPS
//        handler.postDelayed(runnable, 0);
        String mac = getMac(this);
        mac = mac.replaceAll(":", "-");
        Log.e("mac", mac);
        logSenderTask.setMac(mac);
        timer.schedule(logSenderTask, 3000, Config.logPeriod * 60 * 1000l);
        try {
            //???????????????
            Api.InitTopic("", mac);
            setmqttconnect();
//            dialog_dismiss();
        } catch (Exception e) {
//            rebootDeviceTask.run();
            myLog.Write_Log(MyLog.LOG_ERROR, "???????????????" + e.toString());
            Log.e("need_restart", "????????????");
            //??????????????????????????????
            Rerun(this);
//            e.printStackTrace();
        }
        //????????????
        openserialport();
        lockerInfoTask.setMac(mac);
        lockerInfoTask.setCoordinates(coordinates);
        lockerInfoTask.setDevId(Config.devId);
        //?????????????????? 5??????
        int netWorkType = nwct.getNetWorkType();
        lockerInfoTask.setNetWorkType(netWorkType);
        Log.e("netWorkType", netWorkType + "");
        timer.schedule(lockerInfoTask, 5000l, Config.infoPeriod * 1000l);
        //TODO ??????????????????????????????????????????????????????30???????????????
        timer.schedule(aut,31000l,Config.alertPeriod*1000l);
        //?????????????????? 2??????
//        timer.schedule(batteryInfoTask, 0, 1000 * 2 * 60);
        seriaPort.SetHostConfig(hostUpdateData);
//        startCurrentService(this);
    }

    public void startCurrentService(Context context) {
        Intent intent = new Intent(context, MonitoringService.class);
        startService(intent);
    }

    public class RebootDeviceTask extends TimerTask {
        @Override
        public void run() {
            try {
                Thread.sleep(3 * 1000l);
                Intent intent2 = new Intent(Intent.ACTION_REBOOT);
                intent2.putExtra("nowait", 1);
                intent2.putExtra("interval", 1);
                intent2.putExtra("window", 0);
                sendBroadcast(intent2);
            } catch (Exception e) {
                Log.e("restart", "restart failed");
                myLog.Write_Log(MyLog.LOG_ERROR, "????????????" + e.toString());
            }
        }
    }

    private void setmqttconnect() {
        Api.InitData initData = Api.getInitData();
        //??????????????????WiseMqtt???????????????,??????????????????
        MqttManager.getInstance()
                .init(getApplication())
                .setServerIp(initData.GetMqttUrl())
                .setServerPort(Integer.valueOf(initData.GetMqttPort()))
                .connect();
        //???????????????mqtt???????????????????????????
        MqttManager.mHandler.post(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean current = false;
                        try {
                            if (isConnected) {
                                return;
                            }
                            while (!isConnected) {
                                Thread.sleep(1000l);
                                current = MqttManager.getInstance().isConnected();
                                Log.e("subscribe]]]]", "waiting...");
                                Log.e("subscribe]]]]", "this is" + Thread.currentThread().getName());
                                if (current) {
                                    isConnected = true;
                                    MqttManager.getInstance().regeisterServerMsg(mqttAndroidConnectListener);
                                    Log.e("subscribe]]]]", "connecting...");
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }).start();
            }
        });
    }

    private void openserialport() throws IOException {
        //232 ttys4
        //485 ttys1
        seriaPort = new SeriaPort(new File("/dev/ttyS4"), 115200, getSp_data);
        if (seriaPort.isHavePermission()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(2 * 1000);
                            seriaPort.Command_GetFileName(1);
                            Thread.sleep(2 * 1000);
                            seriaPort.Command_GetFileName(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {

        }
    }

    //???????????????????????????????????????????????????
    SeriaPort.GetSp_Data getSp_data = new SeriaPort.GetSp_Data() {
        @Override
        public void getdata(byte[] bd) {
            int message_id = bd[3] & 0xFF;
            if ((bd[4] & 0xFF) + ((bd[5] << 8) & 0xFF00) == 0) {//???????????????
                return;
            }

            byte[] bs = new byte[bd.length - 9];
            System.arraycopy(bd, 7, bs, 0, bs.length);

            String str = new String(bs);
            switch (message_id) {
//                case SerialPort.R_LockerInfo:
//                    serialPort.LockerInfo(null, true);
//                    break;
            }
        }
    };


    /**
     * ??????????????????
     */
    public void sendNotifications(int slotId, String orderNum, String event, String batId) throws JSONException {
        JSONObject jsonobject = new JSONObject();
        jsonobject.put("slot_id", slotId);
        jsonobject.put("order_number", orderNum);
        jsonobject.put("event", event);
        jsonobject.put("battery_id", batId);
        jsonobject.put("timestamp", dfst.format(new Date()));
        MqttManager.getInstance().sendMsg(Api.Topic_W_Notification, jsonobject.toString());
    }

    /**
     * ??????????????????
     */
    private class PhoneStatListener extends PhoneStateListener {
        //??????????????????
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //????????????????????????
            //??????0-4???5????????????????????????????????????,??????api23???????????????
            int level = signalStrength.getLevel();
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            //??????????????????
            int netWorkType = getNetWorkType(getApplicationContext());
            Log.e("networkType", netWorkType + "");
            Log.e("networkLevel", level + "");
        }
    }

    /**
     * ??????????????????
     *
     * @param context ?????????
     * @return
     */
    public static int getNetWorkType(Context context) {
        int mNetWorkType = -1;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return isFastMobileNetwork(context) ? NETWORKTYPE_4G : NETWORKTYPE_2G;
            }
        } else {
            mNetWorkType = NETWORKTYPE_NONE;//????????????
        }
        return mNetWorkType;
    }

    /**
     * ??????????????????
     */
    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //????????????????????????????????????????????????4G???????????????????????????????????????????????????
        return telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    private int stringToInt(String string) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(string);
        return Integer.valueOf(m.replaceAll("").trim());
    }

    private void setCoordinates(Location location) {
        Toast.makeText(getApplicationContext(), "Longitude:" + location.getLongitude() + "\nLatitude:" + location.getLatitude(), Toast.LENGTH_SHORT).show();
        coordinates.setLongitude(location.getLongitude());
        coordinates.setLatitude(location.getLatitude());
    }

    /**
     * ??????????????????app
     */
    public void Rerun(Context context) {
        try {
            startCurrentService(context);
            System.exit(0);
        } catch (Exception e) {
            Log.i("TESTREBOOT", e.toString());
        }
    }


    /**
     * ??????App
     *
     * @param silent ????????????????????? ??????????????????????????????????????????
     */
    private void update_App(boolean silent) {
        myLog.Write_Log(MyLog.LOG_INFO, "App ??????????????????");
        upfileapp = hostUpdateData.getApk().getFilename();
        if (Config.autoUpdateApk) {
            try {
                File file = new File(Config.apkFpath + "/" + Config.apkFname);
                if (silent) {//???????????????????????????
                    auto_install_app(file);
                } else {//??????????????????????????????
                    user_install_app(file);
                }
            } catch (Exception e) {
                myLog.Write_Log(MyLog.LOG_INFO, "????????????:" + e.toString());
                postToOta("Apk", false, e + "", upfileapp);
            }
        } else {
            myLog.Write_Log(MyLog.LOG_INFO, "App???????????????????????????????????????");
        }
    }

    private void auto_install_app(File apkFile) throws IOException {
        boolean b = ShellUtils.checkRootPermission();
        if (b) {
            String apkPath = apkFile.getAbsolutePath();
            Map map = Config.getConfig_map();
            //???????????????apk
            map.put("apkFname", upfileapp);
            Config.Set_Config(map);
            int resultCode = PackageUtils.installSilent(this, apkPath);
            if (resultCode != PackageUtils.INSTALL_SUCCEEDED) {
                myLog.Write_Log(MyLog.LOG_INFO, "App???????????? : " + resultCode);
                counts[0]++;
                //??????????????????????????????
                if (counts[0] <= 2) {
                    postToOta("Apk", false, resultCode + "", upfileapp);
                }
            }
        } else {
            myLog.Write_Log(MyLog.LOG_INFO, "??????root????????????????????????????????????????????????");
        }
    }

    private void user_install_app(File apkFile) {
        Intent intentInstall = new Intent();
        intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentInstall.setAction(Intent.ACTION_VIEW);
        intentInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(this, "com.example.android.zldc.fileProvider", apkFile);
        intentInstall.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intentInstall);
    }

    boolean isMainUpdate, isupdate_all = false;

    /**
     * ????????????
     *
     * @param filename   ?????????
     * @param hw_version ???????????? 0 ???????????????
     * @param bid        ??????ID null ???????????????
     * @param iscomple   ??????????????????
     * @param up_version ???????????????
     */
    private void update_Main(String filename, int hw_version, String bid, boolean iscomple, int up_version, int pro_ver) {

        isupdate_all = true;
        try {
            // ??????????????????
            iscomple = true;

            upfilemain = hostUpdateData.getMain().getFilename();
            myLog.Write_Log(MyLog.LOG_INFO, "????????? ??????????????????");
            seriaPort.setFilename(filename);
            int rt = seriaPort.Command_Update((byte) 1, hw_version, bid, iscomple, up_version, pro_ver, new SeriaPort.Update_interface() {
                @Override
                public void dataTransmission(int type) {
                    String str = "";
                    isMainUpdate = false;
                    switch (type) {
                        case 0:
                            str = "?????????????????????";
                            isMainUpdate = true;
                            break;
                        case 1:
                            str = "?????????????????????";
                            break;
                        case -1:
                            str = "?????????????????????";
                            break;
                        case 2:
                            str = "?????????????????????";
                            break;
                        case 3:
                            break;
                        case 4:
                            str = "???????????????????????????";
                            break;
                        case -8:
                            str = "????????????????????????";
                            break;
                    }
                    if (type != 0) {
                        counts[1]++;
                        if (counts[1] <= 2) {
                            postToOta("??????", false, str, upfilemain);
                            LocalData.Updating u = LocalData.getUpdating();
                            u.setMainUpdating(false);
                            LocalData.setUpdating(u);
                        }
                    }
                }

                @Override
                public void getData(boolean isok, int boxid) {
                    if (isok) {
                        // VOLTZ_ZLD_CH200_P103_S107.bin
                        String version = netUpdateData.getMain().getFilename();
                        if (StringUtils.isNotEmpty(version)) {
                            //????????????
                            String softVerStrP = version.split("_")[4];
                            String softVerStr = softVerStrP.split("\\.")[0];
                            LocalData.getMainData().setMainSoftNum(softVerStr.replace("S","V"));

                            try {
                                seriaPort.Command_GetMainInfo();
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        hostUpdateData.setMain(netUpdateData.getMain());
                        try {
                            Map map = Config.getConfig_map();
                            map.put("mHwVersion", LocalData.getMainData().getChVer()+"");
                            map.put("mSwVersion", LocalData.getMainData().getMainSoftNum());
                            map.put("mProVersion", LocalData.getMainData().getMainProNum());
                            Config.Set_Config(map);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        postToOta("??????", true, "", upfilemain);
                        LocalData.Updating u = LocalData.getUpdating();
                        u.setMainUpdating(false);
                        LocalData.setUpdating(u);
                        myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????");
                    }
                }
            });
            if (rt == SeriaPort.SP_WRITE_READ_ERROE) {
                myLog.Write_Log(MyLog.LOG_INFO, "????????????????????????????????????");
            }
        } catch (Exception e) {
            LocalData.Updating u = LocalData.getUpdating();
            u.setMainUpdating(false);
            LocalData.setUpdating(u);
            myLog.Write_Log(MyLog.LOG_INFO, "??????????????????" + e.toString());
        }
        isupdate_all = false;
    }

    /**
     * ????????????
     */
    private void update_Sub(String filename, int hw_version, String bid, boolean iscomple, int up_version, int pro_ver) {

        isupdate_all = true;

        // ??????????????????
        iscomple = true;

        upfilesub_file = hostUpdateData.getSub().getFilename();

        if (Config.autoUpdateSub) {
            try {
                myLog.Write_Log(MyLog.LOG_INFO, "????????? ??????????????????");

                seriaPort.setFilename(filename);
                int rt = seriaPort.Command_Update((byte) 2, hw_version, bid, iscomple, up_version, pro_ver, new SeriaPort.Update_interface() {
                    @Override
                    public void dataTransmission(int type) {
                        switch (type) {
                            case 0:
                                myLog.Write_Log(MyLog.LOG_INFO, "???????????????????????????");
                                break;
                            case 1:
                                myLog.Write_Log(MyLog.LOG_INFO, "???????????????????????????");
                                break;
                            case -1:
                            case 2:
                            case 3:
                            case 4:
                                myLog.Write_Log(MyLog.LOG_INFO, "???????????????????????????");
                                break;
                        }
                    }

                    @Override
                    public void getData(boolean isok, int boxid) {
                        if (isok) {
                            String version = netUpdateData.getSub().getFilename();
                            postToOta("??????", true, "", upfilesub_file);
                            LocalData.Updating u = LocalData.getUpdating();
                            u.setSubUpdating(false);
                            LocalData.setUpdating(u);
//                            mkDeviceState.setSub_hw_ver(version.split("_")[4]);
//                            mkDeviceState.setSub_sw_ber(version.split("_")[5].split("\\.")[0]);
                            myLog.Write_Log(MyLog.LOG_INFO, "?????????????????????");
                        }
                    }
                });
                if (rt == SeriaPort.SP_WRITE_READ_ERROE) {
                    myLog.Write_Log(MyLog.LOG_INFO, "????????????????????????????????????");
                } else {
                    waitUpdate();
                }
            } catch (Exception e) {
                postToOta("??????", false, "", upfilesub_file);
                LocalData.Updating u = LocalData.getUpdating();
                u.setSubUpdating(false);
                LocalData.setUpdating(u);
                myLog.Write_Log(MyLog.LOG_INFO, "??????????????????" + e.toString());
            }
        }

        isupdate_all = false;
    }

    private void postToOta(String mode, boolean issucess, String error, String upfile, int... box) {
        boolean isUp = false;

        int mv = -1;
        int sv = -1;
        int rv = -1;
        int hmv = -1;
        int hsv = -1;
        int hrv = -1;

        String fn = "";
        String cn = "";
        switch (mode) {
            case "??????":
                mv = netUpdateData.getMain().getMain_ver();
                sv = netUpdateData.getMain().getSub_ver();
                rv = netUpdateData.getMain().getRevision();
                hmv = hostUpdateData.getMain().getMain_ver();
                hsv = hostUpdateData.getMain().getSub_ver();
                hrv = hostUpdateData.getMain().getRevision();
                fn = netUpdateData.getMain().getFilename();
                cn = hostUpdateData.getMain().getFilename();
                break;
            case "??????":
                mv = netUpdateData.getSub().getMain_ver();
                sv = netUpdateData.getSub().getSub_ver();
                rv = netUpdateData.getSub().getRevision();
                hmv = hostUpdateData.getSub().getMain_ver();
                hsv = hostUpdateData.getSub().getSub_ver();
                hrv = hostUpdateData.getSub().getRevision();
                fn = netUpdateData.getSub().getFilename();
                cn = hostUpdateData.getSub().getFilename();
                break;
            case "Apk":
                mv = netUpdateData.getApk().getMain_ver();
                sv = netUpdateData.getApk().getSub_ver();
                rv = netUpdateData.getApk().getRevision();
                hmv = hostUpdateData.getApk().getMain_ver();
                hsv = hostUpdateData.getApk().getSub_ver();
                hrv = hostUpdateData.getApk().getRevision();
                fn = netUpdateData.getApk().getFilename();
                cn = hostUpdateData.getApk().getFilename();
                break;
        }

        if (mv > hmv) {
            isUp = true;
        } else if (mv == hmv) {
            if (sv > hsv) {
                isUp = true;
            } else if (sv == hsv) {
                if (rv > hrv) {
                    isUp = true;
                }
            }
        }

        try {
            toUpdateServer.PostUploadCommon(mode, fn, upfile, issucess, isUp, box.length > 0 ? box[0] : -1, error);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getZldConfigData() {
        toUpdateServer.GetConfigData(new ToUpdateServer.DataCallBack() {
            @Override
            public void getData(Object data, int mode) {
                Log.e("configData", ((UpdateData) data).toString());
                if (!isExchange) {//????????????????????????????????????????????????
                    zld_Response((UpdateData) data, mode, null, 0, null, false, 0);
                }
            }
        });
    }

    private String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = CommonClass.getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = CommonClass.getMacAddress();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = CommonClass.getMacFromHardware();
        }
        return mac;
    }

    private void zld_Response(UpdateData data, int mode, String filename, int hw_version, String bid, boolean iscomple, int up_version) {
        int v1 = 0;
        String fn = "";
        String proStr = "";
        if (StringUtils.isNotBlank(filename)) {
            proStr = filename.split("_")[3].replace("P", "");
        }
        int pro_ver = 0;
        if (StringUtils.isNotBlank(proStr)) {
            pro_ver = Integer.valueOf(proStr);
        }
        switch (mode) {
            case ToUpdateServer.Mode_Config:
                netUpdateData = data;
                print_zldconfig(netUpdateData);
                equals_zldconfig();
                break;
            case ToUpdateServer.Mode_Apk:
                //CDG_MD_C_B1_A100_101.apk
                update_App(true);
                break;
            case ToUpdateServer.Mode_Main:
                //CDG_MD_B_B1_100_117.bin
                try {
                    fn = hostUpdateData.getMain().getFilename();
                    v1 = stringToInt(fn.split("_")[4].split("\\.")[0].replace("S", ""));
                } catch (Exception e) {

                }
                //VOLTZ_ZLD_SH200_P102_S110.bin
                iscomple = v1 > up_version;
                update_Main(filename, hw_version, bid, iscomple, up_version, pro_ver);
                break;
            case ToUpdateServer.Mode_Sub:
                //CDG_MD_A_B1_100_431.bin
                try {
                    fn = hostUpdateData.getSub().getFilename();
                    v1 = stringToInt(fn.split("_")[4].split("\\.")[0].replace("S", ""));
                } catch (Exception e) {

                }
                iscomple = v1 > up_version;
                update_Sub(filename, hw_version, bid, iscomple, up_version, pro_ver);
                break;
        }
    }

    /**
     * ???????????????????????????(?????????????????????????????????)
     */
    private void print_zldconfig(UpdateData data) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("apk \n{")
                .append(data.getApk().toString())
                .append("}")
                .append("\n")
                .append("main \n{")
                .append(data.getMain().toString())
                .append("}")
                .append("\n")
                .append("sub \n{")
                .append(data.getSub().toString())
                .append("}")
                .append("\n");
        myLog.Write_Log(MyLog.LOG_INFO, stringBuilder.toString());
        Log.e("print_zldconfig", stringBuilder.toString());
    }


    /**
     * ????????????????????????????????????????????????
     */
    private void equals_zldconfig() {
        boolean isdownload = false;
        Log.e("netUpdateData", netUpdateData.toString());
        Log.e("hostUpdateData", hostUpdateData.toString());
        UpdateData.Sub netsub = netUpdateData.getSub();
        UpdateData.Sub hostsub = hostUpdateData.getSub();
        if (netsub != null && hostsub != null) {
            boolean isd = equals_Base(ToUpdateServer.Mode_Sub, netsub, hostsub);
            if (!isdownload)
                isdownload = isd;
        }

        if (isdownload) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isdownload) {
            UpdateData.Main netmain = netUpdateData.getMain();
            UpdateData.Main hostmain = hostUpdateData.getMain();
            if (hostmain != null && netmain != null) {
                isdownload = equals_Base(ToUpdateServer.Mode_Main, netmain, hostmain);
            }

            if (isdownload) {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isdownload) {
                UpdateData.Apk netapk = netUpdateData.getApk();
                UpdateData.Apk hostapk = hostUpdateData.getApk();
                if (netapk != null && hostapk != null) {
                    equals_Base(ToUpdateServer.Mode_Apk, netapk, hostapk);
                }
            }
        }
    }

    private boolean equals_Base(int mode, UpdateData.Baseclass net, UpdateData.Baseclass host) {
        boolean download = false;
        String str = "";
        switch (mode) {
            case ToUpdateServer.Mode_Apk:
                str = " Apk ";
                break;
            case ToUpdateServer.Mode_Main:
                str = " ????????? ";
                break;
            case ToUpdateServer.Mode_Sub:
                str = " ????????? ";
                break;
        }

        myLog.Write_Log(MyLog.LOG_INFO, "??????" + str + "?????????:\t\t" +
                host.getFilename() + "\n" +
                "?????????" + str + "?????????:\t\t" +
                net.getFilename());


        if (net.getFilename() != null && !net.getFilename().equals("")) {
            if (!net.getFilename().equals(host.getFilename())) {
                download = true;
                myLog.Write_Log(MyLog.LOG_INFO,
                        str + "??????????????????????????????");
                //??????????????????????????????
                if(LocalData.isExchanging){
                    myLog.Write_Log(MyLog.LOG_INFO,
                            "??????????????????????????????????????????");
                    return false;
                }else{
                    //???????????????????????????????????????
                    LocalData.Updating u=LocalData.getUpdating();
                    switch (mode) {
                        case ToUpdateServer.Mode_Apk:
                            u.setAppUpdating(true);
                            break;
                        case ToUpdateServer.Mode_Main:
                            u.setMainUpdating(true);
                            break;
                        case ToUpdateServer.Mode_Sub:
                            u.setSubUpdating(true);
                            break;
                    }
                    LocalData.setUpdating(u);
                }
                switch (mode) {
                    case ToUpdateServer.Mode_Apk:
                        getZldApk(net.getFilename());
                        break;
                    case ToUpdateServer.Mode_Main:
                        getZldMain(net.getFilename());
                        break;
                    case ToUpdateServer.Mode_Sub:
                        getZldSub(net.getFilename());
                        break;
                }
            }
        }
        return download;
    }

    private void getZldApk(String fname) {
//        if(isHasDownloaded(3, fname)) {
//            UpdateData.Apk apk = netUpdateData.getApk();
//            zld_Response(null, ToUpdateServer.Mode_Apk, apk.getFilename(),
//                    0,null,false,0);
//        } else {
        toUpdateServer.GetApk(fname, new ToUpdateServer.DataCallBack() {
            @Override
            public void getData(Object data, int mode) {
                UpdateData.Apk apk = netUpdateData.getApk();
                zld_Response((UpdateData) data, mode, apk.getFilename(),
                        0, null, false, 0);
            }
        });
//        }
    }

    private void getZldMain(String fname) {
//        if(isHasDownloaded(3, fname)) {
//            UpdateData.Main main = netUpdateData.getMain();
//            String fn = main.getFilename();
//            zld_Response(null, ToUpdateServer.Mode_Main, fn,
//                    stringToInt(fn.split("_")[4]), null, false,
//                    main.getMain_ver()*100 +
//                            main.getSub_ver()*10 +
//                            main.getRevision());
//        } else {
        toUpdateServer.GetMain(fname, new ToUpdateServer.DataCallBack() {
            @Override
            public void getData(Object data, int mode) {
                UpdateData.Main main = netUpdateData.getMain();
                String fn = main.getFilename();
                String hwVerStr = fn.split("_")[2].replace("CH", "");
                int hwVersion = Integer.valueOf(hwVerStr);
                zld_Response((UpdateData) data, mode, fn,
                        hwVersion, null, false,
                        main.getMain_ver() * 100 +
                                main.getSub_ver() * 10 +
                                main.getRevision());
            }
        });
//        }
    }

    private void getZldSub(String fname) {
//        if(isHasDownloaded(3, fname)) {
//            UpdateData.Sub sub = netUpdateData.getSub();
//            String fn = sub.getFilename();
//            zld_Response(null, ToUpdateServer.Mode_Sub, fn,
//                    stringToInt(fn.split("_")[4]), null, false,
//                    sub.getMain_ver()*100 +
//                            sub.getSub_ver()*10 +
//                            sub.getRevision());
//        } else {
        toUpdateServer.GetSub(fname, new ToUpdateServer.DataCallBack() {
            @Override
            public void getData(Object data, int mode) {
                UpdateData.Sub sub = netUpdateData.getSub();
                String fn = sub.getFilename();
                String hwVerStr = fn.split("_")[2].replace("SH", "");
                int hwVersion = Integer.valueOf(hwVerStr);
                zld_Response((UpdateData) data, mode, fn,
                        hwVersion, null, false,
                        sub.getMain_ver() * 100 +
                                sub.getSub_ver() * 10 +
                                sub.getRevision());
            }
        });
//        }
    }

    private void waitUpdate() throws InterruptedException {
        new Thread(new Runnable() {//5????????????????????????
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 300; i++) {
                        Thread.sleep(1000);
//                        if(!SeriaPort.isDataTran) return;
                    }

                    if (updateThread.getState() == Thread.State.WAITING)
                        synchronized (updateThread) {
                            updateThread.notify();
                        }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        synchronized (updateThread) {
            updateThread.wait();
        }
    }

    public static void clearEvent(){
        ctt.setTimeoutEvent("");
    }

    /**
     * ???????????????????????????????????????????????????
     * @param currentEvent  ??????????????????
     * @param newEvent      ?????????????????????
     * @param timeout       ????????????
     */
    public static void clearCurrentEvent(Timer t, CheckTimeoutTask ctt, String currentEvent, String newEvent, int timeout){
        ctt.setTimeoutEvent(currentEvent);
        try{
            Thread.sleep(1000);
            ctt.setTimeoutEvent(newEvent);
            ctt.setTimeOutValue(timeout);
            t.schedule(ctt,0,1000);
        } catch(Exception e){}
    }



}