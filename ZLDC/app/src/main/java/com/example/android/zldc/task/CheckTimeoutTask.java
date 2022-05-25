package com.example.android.zldc.task;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.android.zldc.Api;
import com.example.android.zldc.Config;
import com.example.android.zldc.LocalData;
import com.example.android.zldc.MqttManager;
import com.example.android.zldc.MyLog;
import com.example.android.zldc.util.BuildRandomNumber;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * 事件超时检测
 */
public class CheckTimeoutTask extends TimerTask {
    private SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    MyLog myLog = MyLog.getInstance();

    /**
     * 超时事件
     */
    private String timeoutEvent;
    /**
     * 超时阈值
     */
    private int timeOutValue;
    /**
     * 当前计时,默认从0开始，用于当前任务检查需要提前终止计时
     */
    private int current;

    @Override
    public void run() {
        if (StringUtils.isBlank(timeoutEvent)) {
            current = 0;
//            myLog.Write_Log(MyLog.LOG_INFO,"timeoutTask-没有待检测事件");
            return;
        }
        current++;
//        myLog.Write_Log(MyLog.LOG_INFO,"timeoutTask-当前事件：【" + timeoutEvent + "】,事件超时阈值:【" + timeOutValue + "】,已有【" + current + "】s");
        if (current > timeOutValue) {
            //关空仓和开电池仓需要计时
            try {
                JSONObject jsonobject = new JSONObject();
                Api.BoxOpenComm boxOpenComm = Api.getBoxOpenComm();
                String orederNum = boxOpenComm.getOrderNumber();
                int slotId = boxOpenComm.getSlotId();
                if (timeoutEvent.equals("close_slot_timeout")) {
                    jsonobject.put("battery_id", "");
                    jsonobject.put("soc", 0);
                }else if (timeoutEvent.equals("waiting_open_full_timeout")) {
                    LocalData.Bms bms = LocalData.getBatData(String.valueOf(slotId));
                    if (bms != null) {
                        LocalData.BmsData bd = bms.getBmsData();
                        String batId = bd.getId();
                        int soc = bd.getSoc();
                        jsonobject.put("battery_id", batId);
                        jsonobject.put("soc", soc);
                    }
                }
                jsonobject.put("slot_id", slotId);
                jsonobject.put("order_number", orederNum);
                jsonobject.put("event", timeoutEvent);

                jsonobject.put("timestamp", dfst.format(new Date()));

                //超时之后，订单置空
                Api.BoxOpenComm boc = Api.getBoxOpenComm();
                boc.setOrderNumber("");
                Api.setBoxOpenComm(boc);
                MqttManager.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        Log.e("timeoutTask", jsonobject.toString());
                        MqttManager.getInstance().sendMsg(Api.Topic_W_Notification, jsonobject.toString());
                    }
                });
            }catch(Exception e){

            }
            //已经上报事件，则停止计时，取消当前任务
            current = 0;
            timeoutEvent = "";
            timeOutValue = 0;

        }
    }

    public String getTimeoutEvent() {
        return timeoutEvent;
    }

    public void setTimeoutEvent(String timeoutEvent) {
        this.timeoutEvent = timeoutEvent;
    }

    public int getTimeOutValue() {
        return timeOutValue;
    }

    public void setTimeOutValue(int timeOutValue) {
        this.timeOutValue = timeOutValue;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
