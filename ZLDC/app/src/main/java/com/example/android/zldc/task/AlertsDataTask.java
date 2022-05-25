package com.example.android.zldc.task;

import com.example.android.zldc.LocalData;
import com.example.android.zldc.MyLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 收集告警信息
 */
public class AlertsDataTask extends TimerTask {

    private static final String Alerts="alert";

    private static final String Error="error";

    private SimpleDateFormat dfst = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    private MyLog myLog=MyLog.getInstance();
    @Override
    public void run() {
        LocalData.clearAlertInfos();
        try{
            //主控故障
            LocalData.MainData mainData = LocalData.getMainData();
            boolean[] faultState = mainData.getFaultsState();
            //烟雾故障
            addOrRemove(null,"Smoke sensing alert",Alerts,faultState[0],false);
            boolean[] runningState = mainData.getRuningState();
            //防雷告警
//            addOrRemove(null,"Lightning warning",Alerts,runningState[2],false);

            CopyOnWriteArrayList<LocalData.SubData> subDatas = LocalData.getLocalData().getSubDataList();
            if(subDatas.size()!=8){
                //分控数据不完整，不进行处理
                return;
            }
            //分控存在故障
            for(int i=0;i<subDatas.size();i++){
                LocalData.SubData s = subDatas.get(i);
                //分控运行状态
                boolean[] runState = s.getRuningState();
                LocalData.Bms bms=null;
                LocalData.BmsData bd=null;
                if(runState[0]){
                    bms = LocalData.getBatData(String.valueOf(i+1));
                }
                if(bms!=null){
                    bd = bms.getBmsData();
                }
                LocalData.ChargeData chargeData=null;
                LocalData.ChargeInfo ci=null;
                if(runState[3]){
                    chargeData=LocalData.getChargeData(String.valueOf(i+1));
                }
                if(chargeData!=null){
                    ci= chargeData.getChargeInfo();
                }
                //电池故障
                LocalData.alertBatFault[i]=runState[1];
                if(runState[0]){
                    //从bms里获取具体的故障信息
                    addBmsAlert(i+1,bd,false);
                }else{
                    addBmsAlert(i+1,bd,true);
                }
                //分控故障
                LocalData.alertSlotOTH[i]=runState[2];
                addOrRemove(i+1,"Sub control error",Error,runState[2],false);
                //充电器故障
                LocalData.alertChargerOTH[i]=runState[4];

                addOrRemove(i+1,"Charger communication failure",Error,(!runState[3]&&runState[4]),false);
                addChargeAlert(i+1,ci,false);
                //电池锁故障
                LocalData.alertBatLock[i]=runState[5];
                addOrRemove(i+1,"Battery lock error",Error,runState[5],false);
            }
        }catch(Exception e){
//            myLog.Write_Log(MyLog.LOG_INFO,"AlertsData异常:【"+e.toString()+"】");
        }
//        myLog.Write_Log(MyLog.LOG_INFO,"AlertsData:【\t大小："+LocalData.getAlertInfos().size()+"\t数据："+LocalData.getAlertInfos().toString()+"】");
    }

    private LocalData.AlertInfo setAlertInfo(Integer slotId, String message, String type){
        LocalData.AlertInfo ai=new LocalData().new AlertInfo();
        if(slotId!=null){
            ai.setSlotId(slotId);
        }
        ai.setMessage(message);
        ai.setType(type);
        ai.setTimestamp(dfst.format(new Date()));
        return ai;
    }

    /**
     * 加入或移除充电器故障信息
     * @param slotId 仓
     * @param ci 充电器信息
     * @param remove 移除
     */
    private void addChargeAlert(Integer slotId,LocalData.ChargeInfo ci,boolean remove){
        boolean[] fault=new boolean[16];
        if(ci!=null){
            fault=ci.isFaults();
        }
        addOrRemove(slotId,"Output voltage high",Error,fault[0],remove);
        addOrRemove(slotId,"Average output voltage high",Error,fault[1],remove);
        addOrRemove(slotId,"Battery voltage high",Error,fault[2],remove);
        addOrRemove(slotId,"Average battery voltage high",Error,fault[3],remove);
        addOrRemove(slotId,"Average battery voltage low",Alerts,fault[4],remove);
        addOrRemove(slotId,"Battery reverse connection",Error,fault[5],remove);
        addOrRemove(slotId,"Battery voltage abnormal",Error,fault[6],remove);
        addOrRemove(slotId,"Output overcurrent",Error,fault[7],remove);
        addOrRemove(slotId,"Average output current high",Error,fault[8],remove);
        addOrRemove(slotId,"Charger temperature too high",Error,fault[9],remove);
    }

    /**
     * 加入或移除bms的故障信息
     * @param slotId 当前仓
     * @param bd bms数据
     * @param remove 移除
     */
    private void addBmsAlert(Integer slotId,LocalData.BmsData bd,boolean remove){
        //充电故障
        boolean[] chargInFault = new boolean[16];
        if(bd!=null){
            chargInFault = bd.getChargInFault();
        }
        //充电过压告警  cell over charge warning
        addOrRemove(slotId,"cell over charge warning",Alerts,chargInFault[0],remove);
        //充电过压 cell over charge error
        addOrRemove(slotId,"cell over charge error",Error,chargInFault[1],remove);

        //电池包充电过温告警 pack charge over heat warning
        addOrRemove(slotId,"pack charge over heat warning",Alerts,chargInFault[2],remove);
        //电池包充电过温 pack charge over heat error
        addOrRemove(slotId,"pack charge over heat error",Error,chargInFault[3],remove);

        //电池包充电欠温告警 pack charge low temperatue warning
        addOrRemove(slotId,"pack charge low temperatue warning",Alerts,chargInFault[4],remove);
        //电池包充电欠温 pack charge low temperatue error
        addOrRemove(slotId,"pack charge low temperatue error",Error,chargInFault[5],remove);

        //Pack充电过流告警 pack chare over current warning
        addOrRemove(slotId,"pack chare over current warning",Alerts,chargInFault[6],remove);
        //Pack充电过流 pack chare over current error
        addOrRemove(slotId,"pack chare over current error",Error,chargInFault[7],remove);

        //Pack过压告警 pack over voltage warning
        addOrRemove(slotId,"pack over voltage warning",Alerts,chargInFault[8],remove);
        //Pack过压 pack over voltage error
        addOrRemove(slotId,"pack over voltage error",Error,chargInFault[9],remove);

        //充电机通讯超时 communication error with charger
        addOrRemove(slotId,"communication error with charger",Error,chargInFault[10],remove);
        //PACK充电缓启动故障 pack charge soft start error
        addOrRemove(slotId,"pack charge soft start error",Error,chargInFault[11],remove);
        //充电继电器粘连 charging relay stuck
        addOrRemove(slotId,"charging relay stuck",Error,chargInFault[12],remove);

        //放电故障
        boolean[] chargOutFault =new boolean[16];
        if(bd!=null){
            chargOutFault = bd.getChargOutFault();
        }
//        bit0	cell放电欠压告警 cell discharge under voltage warning
        addOrRemove(slotId,"cell discharge under voltage warning",Alerts,chargOutFault[0],remove);
//        bit1	cell放电欠压 cell discharge under voltage error
        addOrRemove(slotId,"cell discharge under voltage error",Error,chargOutFault[1],remove);
//        bit2	cell深度欠压 cell deep under voltage
        addOrRemove(slotId,"cell deep under voltage",Error,chargOutFault[2],remove);
//        bit3	电池包放电过温告警 pack discharge over heat warning
        addOrRemove(slotId,"pack discharge over heat warning",Alerts,chargOutFault[3],remove);
//        bit4	电池包放电过温 pack discharge over heat error
        addOrRemove(slotId,"pack discharge over heat error",Error,chargOutFault[4],remove);
//        bit5	电池包放电欠温告警 pack discharge low temperature warning
        addOrRemove(slotId,"pack discharge low temperature warning",Alerts,chargOutFault[5],remove);
//        bit6	电池包放电欠温 pack discharge low temperature error
        addOrRemove(slotId,"pack discharge low temperature error",Error,chargOutFault[6],remove);
//        bit7	Pack放电过流告警 pack dischage over current waning
        addOrRemove(slotId,"pack dischage over current waning",Alerts,chargOutFault[7],remove);
//        bit8	Pack放电过流 pack dischage over current error
        addOrRemove(slotId,"pack dischage over current error",Error,chargOutFault[8],remove);
//        bit9	Pack欠压告警 pack under voltage warning
        addOrRemove(slotId,"pack under voltage warning",Alerts,chargOutFault[9],remove);
//        bit10	Pack欠压 pack under voltage  error
        addOrRemove(slotId,"pack under voltage  error",Error,chargOutFault[10],remove);
//        bit11	VCU通讯超时 Communication error to VCU
        addOrRemove(slotId,"Communication error to VCU",Error,chargOutFault[11],remove);
//        bit12	PACK放电缓启动故障  pack discharge soft start error
        addOrRemove(slotId,"pack discharge soft start error",Error,chargOutFault[12],remove);
//        bit13	放电继电器粘连 discharging relay stuck
        addOrRemove(slotId,"discharging relay stuck",Error,chargOutFault[13],remove);
//        bit14	Pack放电短路 pack discharge short
        addOrRemove(slotId,"pack discharge short",Error,chargOutFault[14],remove);


        //通用故障
        boolean[] commonFault = new boolean[16];
        if(bd!=null){
            commonFault = bd.getCommonFault();
        }
//        bit0	PACK温差过大  pack excessive temperature differentials
        addOrRemove(slotId,"pack excessive temperature differentials",Error,commonFault[0],remove);
//        bit1	CELL压差过大 cell excessive voltage differentials
        addOrRemove(slotId,"cell excessive voltage differentials",Error,commonFault[1],remove);
//        bit2	AFE故障 AFE Error
        addOrRemove(slotId,"AFE Error",Error,commonFault[2],remove);
//        bit3	MOS过温 MOS over temperature
        addOrRemove(slotId,"MOS over temperature",Error,commonFault[3],remove);
//        bit4	外部EEPROM失效 external EEPROM failure
        addOrRemove(slotId,"external EEPROM failure",Error,commonFault[4],remove);
//        bit5	RTC失效 RTC failure
        addOrRemove(slotId,"RTC failure",Error,commonFault[5],remove);
//        bit6	并机ID检测异常 ID conflict
        addOrRemove(slotId,"ID conflict",Alerts,commonFault[6],remove);
//        bit7	并机内CAN通讯丢失 CAN message miss
        addOrRemove(slotId,"CAN message miss",Alerts,commonFault[7],remove);
//        bit8	并机压差过大 pack excessive voltage differentials
        addOrRemove(slotId,"pack excessive voltage differentials",Alerts,commonFault[8],remove);
//        bit9	充放电电流方向异常 charge and discharge current conflict
        addOrRemove(slotId,"charge and discharge current conflict",Error,commonFault[9],remove);
//        bit10	并机输出动力线连接异常 cable abnormal
        addOrRemove(slotId,"cable abnormal",Error,commonFault[10],remove);
    }

    /**
     * 根据标准追加或移除告警信息
     * @param slotId 仓
     * @param message 消息
     * @param type 类型
     * @param standard 标准
     * @param remove 移除
     */
    private void addOrRemove(Integer slotId,String message,String type,boolean standard,boolean remove){
        LocalData.AlertInfo ai = setAlertInfo(slotId,message,type);
        if(remove){
            LocalData.getAlertInfos().remove(ai);
            return;
        }
        if(standard){
            LocalData.addAlertInfo(ai);
        }else{
            LocalData.getAlertInfos().remove(ai);
        }
    }
}
