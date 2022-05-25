package com.example.android.zldc.task;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.android.zldc.Api;
import com.example.android.zldc.Config;
import com.example.android.zldc.LocalData;
import com.example.android.zldc.MainActivity;
import com.example.android.zldc.MqttManager;
import com.example.android.zldc.MyLog;
import com.example.android.zldc.bean.Coordinates;
import com.example.android.zldc.sers.MyService;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 柜子信息上报定时任务
 */
public class LockerInfoTask extends TimerTask {
    private MyLog myLog = MyLog.getInstance();
    private SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    /**
     * 设备ID
     */
    private String devId;
    /**
     * 经纬度
     */
    private Coordinates coordinates;
    /**
     * 网络状态（-1：没有网络 0:移动网络 1：wifi）
     */
    private int netWorkType;
    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getNetWorkType() {
        return netWorkType;
    }

    public void setNetWorkType(int netWorkType) {
        this.netWorkType = netWorkType;
    }

    @Override
    public void run() {
        {
            //延时1秒,使mqtt初始化完成
            try {
                Thread.sleep(1000l);
            } catch (Exception e) {
                Log.e("exception", e.toString());
            }
            if(MyService.nwct.getNetWorkType()==-1){
                myLog.Write_Log(MyLog.LOG_INFO,"柜子信息上报失败，没有网络");
                return ;
            }
            uploadNow();
        }
    }

    public void uploadNow(){
        try {
            Thread.sleep(4000l);
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        Api.LockerInfo localInfo = new Api().new LockerInfo();
        localInfo.setMacAddr(StringUtils.isNotBlank(Config.mac)?Config.mac:"");
        localInfo.setDevId(StringUtils.isNotBlank(Config.devId)?Config.devId:"");
        localInfo.setTimestamp(dfst.format(new Date()));
        localInfo.setNetWorkType(MyService.nwct.getNetWorkType());
        Coordinates coordinates = getCoordinates();
        if(coordinates!=null){
            localInfo.setCoordinates(coordinates.toString());
        }
        //主控信息
        LocalData.MainData mainData = LocalData.getMainData();
        //分控信息
        CopyOnWriteArrayList<LocalData.SubData> subList = LocalData.getLocalData().getSubDataList();
        //协议号
        localInfo.setCtrPro(mainData.getMainProNum());
        //软件版本号
        localInfo.setCtrSoftver(mainData.getMainSoftNum());
        boolean[] faults = mainData.getFaultsState();
        boolean isWarning=false;
        for(int i=0;i<faults.length;i++){
            if(faults[i]){
                isWarning=true;
                break;
            }
        }
        //告警状态
        localInfo.setCtrWarning(isWarning);
        //总仓数
        if(subList.size()!=0){
            localInfo.setTotalSlots(subList.size());
        }else{
            localInfo.setTotalSlots(0);
        }
        //空仓数
        int emptySlots=0;
        //分控信息
        List<Api.LockerInfo.Bucket> slots=new ArrayList<>();
        for(int i=0;i<subList.size();i++){
            LocalData.SubData subData = subList.get(i);
            if(subData.getBoxRunStatus()==0){
                emptySlots++;
            }
            Api.LockerInfo.Bucket bucket =new Api().new LockerInfo().new Bucket();
            //id
            bucket.setId(i+1);
            //分控协议号
            bucket.setSubPro(subData.getSubProNum());
            //分控软件版本号
            bucket.setSubSoftver(subData.getSubSoftNum());
            //分控故障  不为0表示有故障
            boolean isError=subData.getSubFaultTag()!=0;
            bucket.setSlotStatus(subData.getBoxStatus());
            bucket.setSubExitErr(isError);
            //电池信息
            Api.LockerInfo.Bucket.Battery battery=new Api().new LockerInfo().new Bucket().new Battery();
            //当前仓的bms信息
            LocalData.Bms bms = LocalData.getBatData(String.valueOf(i+1));
            if(bms!=null){
                LocalData.BmsData bmsData = bms.getBmsData();
                battery.setId(bmsData.getId());
                battery.setMaxChgCur(bmsData.getMaxChargCur());
                battery.setMaxChgVol(bmsData.getMaxChargVol());
                battery.setChargerCtrSW(bmsData.isContrSwitch());
                battery.setCtrWorkMode(bmsData.getContrModel());
                battery.setBatVol(bmsData.getVol());
                battery.setBatCur(bmsData.getCur());
                battery.setBatMaxTemp(bmsData.getMaxTemp());
                battery.setBatMinTemp(bmsData.getMinTemp());
                battery.setSoc(bmsData.getSoc());
                battery.setSoh(bmsData.getSoh());
                battery.setChgingErr(bmsData.getChargInFault());
                battery.setDisChgingErr(bmsData.getChargOutFault());
                battery.setMax_Dischg_Cur(bmsData.getMaxChargOutCur());
                battery.setComm_Err(bmsData.getCommonFault());
                //TODO 此处电池信息尚不完善
                bucket.setBattery(battery);
            }
            slots.add(bucket);
        }
        localInfo.setEmptySlots(emptySlots);
        localInfo.setSlots(slots);
        MqttManager.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("sendLockerInfo",JSON.toJSONString(localInfo));
                MqttManager.getInstance().sendMsg(Api.Topic_W_LockerInfo, JSON.toJSONString(localInfo,
                        SerializerFeature.WriteNullListAsEmpty,//集合->null
                        SerializerFeature.WriteNullNumberAsZero,//数字->0
                        SerializerFeature.WriteNullStringAsEmpty,//字符串->“”
                        SerializerFeature.WriteNullBooleanAsFalse,//布尔->false
                        SerializerFeature.WriteMapNullValue//map->null
                ));
            }
        });
    }
}
