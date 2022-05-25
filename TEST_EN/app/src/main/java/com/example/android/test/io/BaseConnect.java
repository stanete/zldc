package com.example.android.test.io;

import android.util.Log;

import com.example.android.test.Api;
import com.example.android.test.MqttManager;
import com.example.android.test.MyLog;
import com.example.android.test.bean.MqttListenerData;
import com.example.android.test.interfaces.OnMqttAndroidConnectListener;

import java.util.ArrayList;

/**
 * Created by xiaomo
 * Date on  2019/4/14
 *
 * @Desc 所有长连接的base
 */

public abstract class BaseConnect extends Thread {
    public String FORMAT_CLIENT_ID = "MQTT_%s_Android_%s";
    protected String TAG = "BaseConnect";
    private final ArrayList<MqttListenerData> mDataLists = new ArrayList<>();   //界面注册的数据和listener

    private final boolean isDebug = true;
    protected MyLog myLog=MyLog.getInstance();
    protected abstract void startConnect(); //开始连接

    protected abstract boolean isConnected();   //判断是否可连接

    protected abstract void disConnect();   //断开连接

    protected abstract void publish(String topic, String json) throws Exception;  //发送消息

    protected abstract void subscribeTopic(String topic, int qos);   //订阅主题

    public BaseConnect() {

    }

    @Override
    public void run() {
        super.run();
        startConnect();
    }

    //发送无须在本次回应中回调的消息
    public void sendMsg(String topic, String body) {
        try {
            publish(topic, body);
        } catch (Exception e) {
            Log.e(TAG, "send msg Exception = " + e.toString());
        }
    }

    public void subscribe(String topic, int qos) {
        try {
            subscribeTopic(topic, qos);
        } catch (Exception e) {
            Log.e(TAG, "subscribeTopic = " + e.toString());
        }
    }


    public void regeisterServerMsg(OnMqttAndroidConnectListener listener) {
        MqttListenerData data = new MqttListenerData();
        data.setListener(listener);
        mDataLists.add(data);
        showLog("regeister event " + data.getEvent() + " + listener" + data.getListener());
    }

    public void unRegeisterServerMsg(OnMqttAndroidConnectListener listener) {
        for (int i = 0; i < mDataLists.size(); i++) {
            MqttListenerData data = mDataLists.get(i);
            if (data.getListener() == listener) {
                showLog("unregeister event " + data.getEvent() + " + listener" + data.getListener() + " + size = " + mDataLists.size());
                mDataLists.remove(i--);
            }
        }
    }

    /**
     * 对相应注册回调模块进行消息回调
     */
    protected void onDataReceiveCallBack(final String topic, final String message) {
        Log.e("onDataReceiveCallBack","topic:"+topic+"\t"+"message:"+message);
        Log.e("mDataLists",mDataLists.size()+"");
        for (int i = 0; i < mDataLists.size(); i++) {
            final MqttListenerData data = mDataLists.get(i);
            Log.e("data",data.toString()+"");
            MqttManager.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    data.getListener().onDataReceive(topic, message);
                }
            });
        }
    }

    /**
     * 断开连接的回调
     */
    protected void disConnectCallBack() {
        ArrayList<OnMqttAndroidConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnMqttAndroidConnectListener onMqttAndroidConnectListener = totalLists.get(i);
            MqttManager.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onMqttAndroidConnectListener.disConnect();
                }
            });
        }
    }

    /**
     * 连接错误会对所有listener进行回调
     */
    protected void connectFailCallBack(final String exception) {
        showLog("disConnectCallBack = " + exception + " connected state = " + isConnected());
        ArrayList<OnMqttAndroidConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnMqttAndroidConnectListener onWebSocketConnectListener = totalLists.get(i);
            MqttManager.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketConnectListener.onConnectFail(exception);
                }
            });
        }
    }

    /**
     * 连接成功会对所有listener进行回调
     */
    protected void connectSuccessCallBack(boolean reconnect) {
        MqttManager.getInstance().subscribe(Api.Topic_R_OpenBox, 2);
        MqttManager.getInstance().subscribe(Api.Topic_R_Thresholds, 2);
        ArrayList<OnMqttAndroidConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnMqttAndroidConnectListener onWebSocketConnectListener = totalLists.get(i);
            MqttManager.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketConnectListener.connect();
                }
            });
        }
    }

    private ArrayList<OnMqttAndroidConnectListener> getAvailListener() {
        ArrayList<OnMqttAndroidConnectListener> list = new ArrayList<>();
        for (int i = 0; i < mDataLists.size(); i++) {   //去掉重复的回调
            MqttListenerData data = mDataLists.get(i);
            if (data.getListener() == null) {
                showLog(" data.getListener() == null");
                continue;
            }
            if (list.contains(data.getListener())) {
                continue;
            }
            list.add(data.getListener());
        }
        return list;
    }

    protected void showLog(String msg) {
        if (isDebug) Log.i(TAG, "" + msg);
    }

}
