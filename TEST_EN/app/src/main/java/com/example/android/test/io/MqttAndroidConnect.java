package com.example.android.test.io;

import android.util.Log;
import android.widget.Toast;

import com.example.android.test.Api;
import com.example.android.test.Config;
import com.example.android.test.LocalData;
import com.example.android.test.MqttManager;
import com.example.android.test.MyLog;
import com.example.android.test.task.HashReceivedCheckTask;
import com.example.android.test.util.BuildRandomNumber;
import com.example.android.test.util.GzipUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;

import javax.net.ssl.SSLContext;

/**
 * Created by xiaomo
 * Date on  2019/4/14
 *
 * @Desc 使用AndroidClient的Mqtt模块, 用以替代使用MqttClient的模块
 */

public class MqttAndroidConnect extends BaseConnect {
    private MqttAndroidClient mqttAndroidClient;
    private HashReceivedCheckTask hashReceivedCheckTask;
    private Timer timer;
    public MqttAndroidConnect() {
        TAG = "MqttAndroidConnect";
        hashReceivedCheckTask=new HashReceivedCheckTask();
        timer=new Timer();
        timer.schedule(hashReceivedCheckTask,1500l,2000l);
    }

    public interface Reconnect {

    }

    @Override
    protected void startConnect() {
        try {
            final long startTime = System.currentTimeMillis();
            String URL_FORMAT = "ssl://%s:%d";
            String randomId = BuildRandomNumber.createGUID();
            String clientId = String.format(FORMAT_CLIENT_ID, MqttManager.APP_NAME, randomId);
            showLog("clientId = " + clientId);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            showLog(String.format(URL_FORMAT, MqttManager.ip, MqttManager.port));
            mqttAndroidClient = new MqttAndroidClient(MqttManager.mApp.getApplicationContext(),
                    String.format(URL_FORMAT, MqttManager.ip, MqttManager.port), clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.e(TAG, "connectComplete = " + reconnect);
                    if (reconnect) {
                        // Because Clean Session is true, we need to re-subscribe
                        showLog("It is reconnect = " + reconnect);
                    } else {
                        showLog("It is first connect...");
                    }
                    connectSuccessCallBack(reconnect);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "connectionLost");
                    showLog("connectionLost = " + cause.getMessage());
                    myLog.Write_Log(MyLog.LOG_INFO,"mqtt连接丢失");
                    disConnectCallBack();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.e("messageArrived",message.toString());
                    if(Config.enableGzip){
                        onDataReceiveCallBack(topic, GzipUtils.decompressForGzip(message.toString()));
                    }else{
                        onDataReceiveCallBack(topic, message.toString());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        MqttMessage message = token.getMessage();
                        String msg="";
                        if(Config.enableGzip){
                            msg = new String(GzipUtils.decompressForGzip(new String(message.getPayload())));
                        }else{
                            msg = new String(message.getPayload());
                        }
                        showLog("deliveryComplete");
                        Log.e("recivedHash",msg.hashCode()+"");
                        LocalData.getLocalData().getHashCheck().revicvedHash(msg.hashCode());
                        myLog.Write_Log(MyLog.LOG_INFO,"接收到投递的消息："+msg);
                    }catch(Exception e){

                    }
                }
            });
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            //断开后，是否自动连接
            mqttConnectOptions.setAutomaticReconnect(true);
            //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
            mqttConnectOptions.setCleanSession(true);
            //设置超时时间，单位为秒
            mqttConnectOptions.setConnectionTimeout(10);
            //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
            mqttConnectOptions.setKeepAliveInterval(10);
            //允许同时发送几条消息（未收到broker确认信息）
            mqttConnectOptions.setMaxInflight(10);
            Api.InitData initData = Api.getInitData();
            mqttConnectOptions.setUserName(initData.GetMqttAdmin());
            mqttConnectOptions.setPassword(initData.GetMqttPassword().toCharArray());
            //开启SSL --开始
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            mqttConnectOptions.setSocketFactory(sslContext.getSocketFactory());
            //开启SSL --结束

            //选择MQTT版本
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "connectSuccess spend time = " + (System.currentTimeMillis() - startTime));
                    showLog("connectSuccess spend time = " + (System.currentTimeMillis() - startTime));
                    myLog.Write_Log(MyLog.LOG_INFO,"mqtt连接耗时"+(System.currentTimeMillis() - startTime));
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showLog("connect failure = " + exception.getMessage());
                    myLog.Write_Log(MyLog.LOG_INFO,"mqtt连接失败:"+exception.getMessage());
                    connectFailCallBack(exception.getMessage());
                    Log.e(TAG, "connect failure = " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception = " + e.toString());
            Toast.makeText(MqttManager.mApp.getApplicationContext(), "错误了", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void subscribeTopic(String topic, int qos) {
        try {
            mqttAndroidClient.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "subscribe topic exception = " + e.toString());
        }
    }

    @Override
    public boolean isConnected() {
        if (mqttAndroidClient == null) {
            return false;
        }
        return mqttAndroidClient.isConnected();
    }

    @Override
    public void disConnect() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.close();
                disConnectCallBack();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void publish(String topic, String msg) throws Exception {
        MqttMessage mqttMsg = new MqttMessage();
        //mqttMsg.setPayload(msg.getBytes());
        if(Config.enableGzip){
            mqttMsg.setPayload(GzipUtils.compressForGzip(msg).getBytes());
        }else{
            mqttMsg.setPayload(msg.getBytes());
        }
        Log.e("sendHash",msg.hashCode()+"");
        IMqttDeliveryToken token = mqttAndroidClient.publish(topic, mqttMsg);
        LocalData.getLocalData().getHashCheck().addMessages(topic,msg);
        MqttMessage message = token.getMessage();
        Log.e("linkany_result", message.toString());
    }

}
