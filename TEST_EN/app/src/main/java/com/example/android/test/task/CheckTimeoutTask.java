package com.example.android.test.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.android.test.Api;
import com.example.android.test.MqttManager;
import com.example.android.test.MyLog;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.TimerTask;

/**
 * 事件超时检测
 */
public class CheckTimeoutTask extends TimerTask {

    private MyLog mylog=MyLog.getInstance();

    private SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

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
            mylog.Write_Log(MyLog.LOG_INFO,"timeoutTask--【没有待检测事件】");
            return;
        }
        current++;
        mylog.Write_Log(MyLog.LOG_INFO,"timeoutTask--当前事件：【" + timeoutEvent + "】,事件超时阈值:【" + timeOutValue + "】,已有【" + current + "】s");
        if (current > timeOutValue) {

            Api.BoxOpenComm boc = Api.getBoxOpenComm();

            //超时之后，订单置空
            Api.getBoxOpenComm().setOrderNumber("");

            MqttManager.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mylog.Write_Log(MyLog.LOG_INFO,"timeoutTask--【"+JSON.toJSONString(null)+"】");
                    MqttManager.getInstance().sendMsg(Api.Topic_W_Notification, JSON.toJSONString(null,
                            SerializerFeature.WriteNullListAsEmpty,//集合->null
                            SerializerFeature.WriteNullNumberAsZero,//数字->0
                            SerializerFeature.WriteNullStringAsEmpty,//字符串->“”
                            SerializerFeature.WriteNullBooleanAsFalse,//布尔->false
                            SerializerFeature.WriteMapNullValue//map->null
                    ));
                }
            });
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
