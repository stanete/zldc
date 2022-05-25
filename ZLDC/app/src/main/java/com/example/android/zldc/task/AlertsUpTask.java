package com.example.android.zldc.task;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.android.zldc.Api;
import com.example.android.zldc.LocalData;
import com.example.android.zldc.MqttManager;
import com.example.android.zldc.MyLog;

import java.util.Set;
import java.util.TimerTask;

/**
 * 故障信息上报
 */
public class AlertsUpTask extends TimerTask {
    private MyLog myLog=MyLog.getInstance();
    @Override
    public void run() {
        Set<LocalData.AlertInfo> ais = LocalData.getAlertInfos();
        if(ais.size()==0){
//            myLog.Write_Log(MyLog.LOG_INFO,"no alerts");
            return;
        }
        if(!LocalData.isNetworking){
//            myLog.Write_Log(MyLog.LOG_INFO,"alerts network error");
            return;
        }
        try{
            for(LocalData.AlertInfo ai:ais){
                MqttManager.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        myLog.Write_Log(MyLog.LOG_INFO,"alertup_【" +JSON.toJSONString(ai)+"】");
                        MqttManager.getInstance().sendMsg(Api.Topic_W_Alerts, JSON.toJSONString(ai,
                                SerializerFeature.WriteNullListAsEmpty,//集合->null
                                SerializerFeature.WriteNullStringAsEmpty,//字符串->“”
                                SerializerFeature.WriteNullBooleanAsFalse,//布尔->false
                                SerializerFeature.WriteMapNullValue//map->null
                        ));
                    }
                });
                Thread.sleep(300l);
            }
        }catch(Exception e){
            myLog.Write_Log(MyLog.LOG_INFO,"发布故障异常："+e.toString());
        }
    }
}
