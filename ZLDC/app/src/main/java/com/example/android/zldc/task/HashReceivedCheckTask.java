package com.example.android.zldc.task;

import com.example.android.zldc.LocalData;
import com.example.android.zldc.MqttManager;
import com.example.android.zldc.MyLog;

import java.util.List;
import java.util.TimerTask;

/**
 * 检测是否收到回复任务
 */
public class HashReceivedCheckTask extends TimerTask {
    private MyLog myLog=MyLog.getInstance();
    @Override
    public void run() {
        LocalData.HashCheck hc = LocalData.getLocalData().getHashCheck();
        List<Integer> sendHash = hc.getSendHash();
        try{
            for (Integer sh:sendHash) {
                String topic = hc.getTopicOfHash(sh);
                String message = hc.getMessageOfHash(sh);
                StringBuffer sb = new StringBuffer();
                if(sh.equals(Integer.valueOf(0))){
                    return;
                }
                sb.append("主题：\t");
                sb.append(topic);
                sb.append("消息内容：\t");
                sb.append(message);
                sb.append("是否发送成功:\t");
                boolean isReceived = hc.checkIsReceived(sh);
                if(!isReceived)
                {
                    sb.append("否\t");
                    MqttManager.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myLog.Write_Log(MyLog.LOG_INFO,sb.toString()+"\tresending");
                            MqttManager.getInstance().sendMsg(topic,message);
                        }
                    });
                }else{
                    sb.append("\t是");
                    myLog.Write_Log(MyLog.LOG_INFO,sb.toString());
                }
            }
        }catch(Exception e){
            myLog.Write_Log(MyLog.LOG_INFO,"重发消息异常："+e.toString());
        }
    }
}
