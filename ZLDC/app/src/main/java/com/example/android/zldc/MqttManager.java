package com.example.android.zldc;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.android.zldc.interfaces.Imanager;
import com.example.android.zldc.interfaces.OnMqttAndroidConnectListener;
import com.example.android.zldc.io.MqttAndroidConnect;

/**
 * Created by xiaomo
 * Date on  2019/4/14 16:25.
 *
 * @Desc mqttdemo 长连接接收推送 模块 的启动,功能操作类
 */

public class MqttManager implements Imanager {
    private static final String TAG = "MqttManager";
    public static String APP_NAME = "MyMqttDemo";
    public static String ip;
    public static int port;
    public static Handler mHandler;
    public static Application mApp; //当前应用的Application

    private MyLog myLog=MyLog.getInstance();
    private final MqttAndroidConnect mMqttAndroidConnect;
    private static MqttManager mInstance;

    public static MqttManager getInstance() {
        synchronized (MqttManager.class) {
            if (mInstance == null) {
                mInstance = new MqttManager();
            }
        }
        return mInstance;
    }

    private MqttManager() {
        mMqttAndroidConnect = new MqttAndroidConnect();

    }

    /**
     * @param application 当前Application
     */
    public MqttManager init(Application application) {
        mApp = application;
        mHandler = new Handler(application.getMainLooper());
        return mInstance;
    }

    /**
     * @param serverIp 服务端的ip
     * @return
     */
    public MqttManager setServerIp(String serverIp) {
        ip = serverIp;
        return mInstance;
    }

    /**
     * @param serverPort 服务端的port
     * @return
     */
    public MqttManager setServerPort(int serverPort) {
        port = serverPort;
        return mInstance;
    }

    @Override
    public void connect() {
        if (mMqttAndroidConnect.isAlive()) {
            Log.e(TAG, "MqttManager connect thread has alive");
            return;
        }
        if (mMqttAndroidConnect.isConnected()) {
            Log.e(TAG, "MqttManager has connected");
            return;
        }
        mMqttAndroidConnect.start();
    }

    @Override
    public void disConnect() {
        if (mMqttAndroidConnect == null) {
            Log.e(TAG, "Wisepush should connect first");
            return;
        }
        mMqttAndroidConnect.disConnect();
    }

    /**
     * 需要订阅的模块的String
     */
    @Override
    public void regeisterServerMsg(OnMqttAndroidConnectListener listener) {
        mMqttAndroidConnect.regeisterServerMsg(listener);
    }

    @Override
    public void unRegeisterServerMsg(OnMqttAndroidConnectListener listener) {
        mMqttAndroidConnect.unRegeisterServerMsg(listener);
    }

    /**
     * @param topic 需要发送的模块的toppic
     */
    @Override
    public void sendMsg(String topic, String message) {
        if (!isConnected()) {
            LocalData.isConnectService=false;
            LocalData.getLocalData().getHashCheck().addMessages(topic,message);
            myLog.Write_Log(MyLog.LOG_INFO,"mqtt尚未建立连接");
//            Toast.makeText(MqttManager.mApp.getApplicationContext(), "还未建立连接", Toast.LENGTH_SHORT).show();
            return;
        }
        mMqttAndroidConnect.sendMsg(topic, message);
        myLog.Write_Log(MyLog.LOG_INFO,"发布主题："+topic+"\n主题内容:"+message);
        Log.i("SENDMSG", topic + " >>> " + message);
    }

    public void subscribe(String topic, int qos) {
        if (!isConnected()) {
            LocalData.isConnectService=false;
            myLog.Write_Log(MyLog.LOG_INFO,"mqtt尚未建立连接");
//            Toast.makeText(MqttManager.mApp.getApplicationContext(), "还未建立连接", Toast.LENGTH_SHORT).show();
            return;
        }
        mMqttAndroidConnect.subscribe(topic, qos);
    }

    /**
     * 判断是否正在连接
     *
     * @return
     */
    @Override
    public boolean isConnected() {
        if (mMqttAndroidConnect == null) {
            return false;
        }
        return mMqttAndroidConnect.isConnected();
    }
}
