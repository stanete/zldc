package com.example.android.test;


import com.alibaba.fastjson.annotation.JSONField;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiaomo
 * Date on  2019/4/14 16:20
 *
 * @Desc
 */
public class Api {
    //终端需获取设备MacAddr地址，上次MacAddr地址进行平台备案
    public static String Mac;//TODO 正式环境需要备案才能使用
    public static String ServerUrl = "www.linkany.net:9502/Client/InitClient";
    public static String Company_name = "zld";
    public static String Download_path = "/sdcard" + "/zld" + "/download";
    public static String Bluetooth_Key;
    //所有主题 终端订阅
    /**
     * 开仓
     */
    public static String Topic_R_OpenBox;
    /**
     * 阈值下发
     */
    public static String Topic_R_Thresholds;
    //所有主题 终端发布
    /**
     * 柜子信息
     */
    public static String Topic_W_LockerInfo;
    /**
     * 通知
     */
    public static String Topic_W_Notification;
    /**
     * 阈值响应
     */
    public static String Topic_W_ThresholdsResponse;
    /**
     * 警告
     */
    public static String Topic_W_Alerts;


    /**
     * 开箱指令
     */
    public static BoxOpenComm boxOpenComm;

    //本地柜子信息!!!!!!!!
    public static LockerInfo lockerInfo;
    //初始化信息
    private static InitData initData;
    public static void InitTopic(String baseUrl,String mac) {

        String str=baseUrl+"/stations";

        Topic_R_OpenBox=str+"/open_slot/"+mac;

        Topic_R_Thresholds=str+"/thresholds/"+mac;

        Topic_W_LockerInfo=str+"/info/"+mac;

        Topic_W_Notification=str+"/notifications/"+mac;

        Topic_W_ThresholdsResponse=str+"/thresholds_response/"+mac;

        Topic_W_Alerts=str+"/alerts/"+mac;
    }

    public static InitData getInitData() {
        if (initData == null) {
            initData = new Api().new InitData();
        }
        return initData;
    }

    public static BoxOpenComm getBoxOpenComm(){
        if(boxOpenComm==null){
            boxOpenComm=new Api().new BoxOpenComm();
        }
        return boxOpenComm;
    }

    /**
     * 平台下发开仓指令
     */
    public class BoxOpenComm{
        /**
         * 仓id
         */
        private int slotId;
        /**
         * 仓信息
         */
        private int slotInfo;
        /**
         * 订单号
         */
        private String orderNumber;
        /**
         * 操作时间
         */
        private String timestamp;

        public void setAllData(JSONObject jsonObject) throws JSONException{
            this.slotId = jsonObject.getInt("slot_id");
            this.slotInfo=jsonObject.getInt("slot_info");
            this.orderNumber=jsonObject.getString("order_number");
            this.timestamp=jsonObject.getString("timestamp");
        }

        public int getSlotId() {
            return slotId;
        }

        public void setSlotId(int slotId) {
            this.slotId = slotId;
        }

        public int getSlotInfo() {
            return slotInfo;
        }

        public void setSlotInfo(int slotInfo) {
            this.slotInfo = slotInfo;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "BoxOpenComm{" +
                    "slotId=" + slotId +
                    ", slotInfo=" + slotInfo +
                    ", orderNumber='" + orderNumber + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }

    public static LockerInfo getLockerInfo(){
        if(lockerInfo==null){
            lockerInfo=new Api().new LockerInfo();
        }
        return lockerInfo;
    }
    /**
     * 柜子信息，所涉及到的信息较多，包括主控、分控、电池、充电器信息在内
     */
    public class LockerInfo{
        /**
         * mac地址
         */
        @JSONField(name="mac_addr")
        private String macAddr;
        /**
         * 设备ID
         */
        @JSONField(name="device_id")
        private String devId;
        /**
         * 经纬度坐标
         */
        @JSONField(name="coordinates")
        private String coordinates;
        /**
         * 主控协议号
         */
        @JSONField(name="ctr_pro")
        private String ctrPro;
        /**
         * 主控软件版本号
         */
        @JSONField(name="ctr_softver")
        private String ctrSoftver;
        /**
         * 主控警告
         */
        @JSONField(name="ctr_warning")
        private boolean ctrWarning;
        /**
         * 总数
         */
        @JSONField(name="total_slots")
        private int totalSlots;
        /**
         * 空仓数
         */
        @JSONField(name="empty_slots")
        private int emptySlots;
        /**
         * 所有仓信息
         */
        @JSONField(name="slots")
        private List<Bucket> slots;
        /**
         * 网络状态（-1：没有网络  0：移动网络  1：wifi）
         */
        @JSONField(name="netWorkType")
        private int netWorkType;
        /**
         * 时间戳
         */
        @JSONField(name="timestamp")
        private String timestamp;

        public int getNetWorkType() {
            return netWorkType;
        }

        public void setNetWorkType(int netWorkType) {
            this.netWorkType = netWorkType;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getMacAddr() {
            return macAddr;
        }

        public void setMacAddr(String macAddr) {
            this.macAddr = macAddr;
        }

        public String getDevId() {
            return devId;
        }

        public void setDevId(String devId) {
            this.devId = devId;
        }

        public String getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(String coordinates) {
            this.coordinates = coordinates;
        }

        public String getCtrPro() {
            return ctrPro;
        }

        public void setCtrPro(String ctrPro) {
            this.ctrPro = ctrPro;
        }

        public String getCtrSoftver() {
            return ctrSoftver;
        }

        public void setCtrSoftver(String ctrSoftver) {
            this.ctrSoftver = ctrSoftver;
        }

        public boolean isCtrWarning() {
            return ctrWarning;
        }

        public void setCtrWarning(boolean ctrWarning) {
            this.ctrWarning = ctrWarning;
        }

        public int getTotalSlots() {
            return totalSlots;
        }

        public void setTotalSlots(int totalSlots) {
            this.totalSlots = totalSlots;
        }

        public int getEmptySlots() {
            return emptySlots;
        }

        public void setEmptySlots(int emptySlots) {
            this.emptySlots = emptySlots;
        }

        public List<Bucket> getSlots() {
            return slots;
        }

        public void setSlots(List<Bucket> slots) {
            this.slots = slots;
        }

        /**
         * 仓信息
         */
        public class Bucket{
            /**
             * 分控协议号
             */
            @JSONField(name="sub_pro")
            private String subPro;
            /**
             * 分控软件版本号
             */
            @JSONField(name="sub_softver")
            private String subSoftver;
            /**
             * 仓门id
             */
            @JSONField(name="id")
            private int id;
            /**
             * 分控是否存在错误
             */
            @JSONField(name="sub_Exit_Err")
            private boolean subExitErr;
            /**
             * 当前仓的电池信息
             */
            @JSONField(name="battery")
            private Battery battery;
            @JSONField(name="slot_status")
            private int slotStatus;

            public int getSlotStatus() {
                return slotStatus;
            }

            public void setSlotStatus(int slotStatus) {
                this.slotStatus = slotStatus;
            }

            public String getSubPro() {
                return subPro;
            }

            public void setSubPro(String subPro) {
                this.subPro = subPro;
            }

            public String getSubSoftver() {
                return subSoftver;
            }

            public void setSubSoftver(String subSoftver) {
                this.subSoftver = subSoftver;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public boolean isSubExitErr() {
                return subExitErr;
            }

            public void setSubExitErr(boolean subExitErr) {
                this.subExitErr = subExitErr;
            }

            public Battery getBattery() {
                return battery;
            }

            public void setBattery(Battery battery) {
                this.battery = battery;
            }

            /**
             * 电池信息
             */
            public class Battery{
                @JSONField(name="id")
                private String id;
                /**
                 * 最大充电电压
                 */
                @JSONField(name="max_ChgVol")
                private float maxChgVol;
                /**
                 * 最大充电电流
                 */
                @JSONField(name="max_ChgCur")
                private float maxChgCur;
                /**
                 * 充电机控制开关
                 */
                @JSONField(name="charger_CtrSW")
                private boolean chargerCtrSW;
                /**
                 *控制工作模式
                 */
                @JSONField(name="ctr_WorkMode")
                private int ctrWorkMode;
                /**
                 * 电池电压
                 */
                @JSONField(name="bat_Vol")
                private float batVol;
                /**
                 * 电池电流
                 */
                @JSONField(name="bat_Cur")
                private float batCur;
                /**
                 * 电池最高温度
                 */
                @JSONField(name="bat_MaxTemp")
                private int batMaxTemp;
                /**
                 * 电池最低温度
                 */
                @JSONField(name="bat_MinTemp")
                private int batMinTemp;
                /**
                 * soc
                 */
                @JSONField(name="soc")
                private int soc;
                /**
                 * soh
                 */
                @JSONField(name="soh")
                private int soh;
                /**
                 * 充电故障
                 */
                @JSONField(name="chging_Err")
                private boolean[] chgingErr=new boolean[16];
                /**
                 * 放电故障
                 */
                @JSONField(name="disChging_Err")
                private boolean[] disChgingErr=new boolean[16];
                /**
                 * 最大放电电流
                 */
                @JSONField(name="max_Dischg_Cur")
                private float max_Dischg_Cur;
                /**
                 * 通用故障
                 */
                @JSONField(name="comm_Err")
                private boolean[] comm_Err=new boolean[16];

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public boolean isChargerCtrSW() {
                    return chargerCtrSW;
                }

                public float getMaxChgVol() {
                    return maxChgVol;
                }

                public void setMaxChgVol(float maxChgVol) {
                    this.maxChgVol = maxChgVol;
                }

                public float getMaxChgCur() {
                    return maxChgCur;
                }

                public void setMaxChgCur(float maxChgCur) {
                    this.maxChgCur = maxChgCur;
                }

                public boolean getChargerCtrSW() {
                    return chargerCtrSW;
                }

                public void setChargerCtrSW(boolean chargerCtrSW) {
                    this.chargerCtrSW = chargerCtrSW;
                }

                public int getCtrWorkMode() {
                    return ctrWorkMode;
                }

                public void setCtrWorkMode(int ctrWorkMode) {
                    this.ctrWorkMode = ctrWorkMode;
                }

                public float getBatVol() {
                    return batVol;
                }

                public void setBatVol(float batVol) {
                    this.batVol = batVol;
                }

                public float getBatCur() {
                    return batCur;
                }

                public void setBatCur(float batCur) {
                    this.batCur = batCur;
                }

                public int getBatMaxTemp() {
                    return batMaxTemp;
                }

                public void setBatMaxTemp(int batMaxTemp) {
                    this.batMaxTemp = batMaxTemp;
                }

                public int getBatMinTemp() {
                    return batMinTemp;
                }

                public void setBatMinTemp(int batMinTemp) {
                    this.batMinTemp = batMinTemp;
                }

                public int getSoc() {
                    return soc;
                }

                public void setSoc(int soc) {
                    this.soc = soc;
                }

                public int getSoh() {
                    return soh;
                }

                public void setSoh(int soh) {
                    this.soh = soh;
                }

                public boolean[] getChgingErr() {
                    return chgingErr;
                }

                public void setChgingErr(boolean[] chgingErr) {
                    this.chgingErr = chgingErr;
                }

                public boolean[] getDisChgingErr() {
                    return disChgingErr;
                }

                public void setDisChgingErr(boolean[] disChgingErr) {
                    this.disChgingErr = disChgingErr;
                }

                public float getMax_Dischg_Cur() {
                    return max_Dischg_Cur;
                }

                public void setMax_Dischg_Cur(float max_Dischg_Cur) {
                    this.max_Dischg_Cur = max_Dischg_Cur;
                }

                public boolean[] getComm_Err() {
                    return comm_Err;
                }

                public void setComm_Err(boolean[] comm_Err) {
                    this.comm_Err = comm_Err;
                }

                @Override
                public String toString() {
                    return "Battery{" +
                            ", maxChgVol='" + maxChgVol + '\'' +
                            ", maxChgCur='" + maxChgCur + '\'' +
                            ", chargerCtrSW='" + chargerCtrSW + '\'' +
                            ", ctrWorkMode='" + ctrWorkMode + '\'' +
                            ", batVol='" + batVol + '\'' +
                            ", batCur='" + batCur + '\'' +
                            ", batMaxTemp='" + batMaxTemp + '\'' +
                            ", batMinTemp='" + batMinTemp + '\'' +
                            ", soc=" + soc +
                            ", soh=" + soh +
                            ", chgingErr=" + chgingErr +
                            ", disChgingErr=" + disChgingErr +
                            ", max_Dischg_Cur='" + max_Dischg_Cur + '\'' +
                            ", comm_Err='" + comm_Err + '\'' +
                            '}';
                }
            }

            @Override
            public String toString() {
                return "Bucket{" +
                        "subPro='" + subPro + '\'' +
                        ", subSoftver='" + subSoftver + '\'' +
                        ", id=" + id +
                        ", subExitErr=" + subExitErr +
                        ", battery=" + battery +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "LockerInfo{" +
                    "macAddr='" + macAddr + '\'' +
                    ", devId='" + devId + '\'' +
                    ", coordinates=" + coordinates +
                    ", ctrPro='" + ctrPro + '\'' +
                    ", ctrSoftver='" + ctrSoftver + '\'' +
                    ", ctrWarning=" + ctrWarning +
                    ", totalSlots=" + totalSlots +
                    ", emptySlots=" + emptySlots +
                    ", slots=" + slots +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockerInfo that = (LockerInfo) o;
            return ctrWarning == that.ctrWarning && totalSlots == that.totalSlots && emptySlots == that.emptySlots && macAddr.equals(that.macAddr) && devId.equals(that.devId) && coordinates.equals(that.coordinates) && ctrPro.equals(that.ctrPro) && ctrSoftver.equals(that.ctrSoftver) && slots.equals(that.slots) && timestamp.equals(that.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(macAddr, devId, coordinates, ctrPro, ctrSoftver, ctrWarning, totalSlots, emptySlots, slots, timestamp);
        }
    }

    /**
     * mqtt初始化连接数据
     */
    public class InitData {
        private String LockerNo;
        private String MqttUrl;
        private String MqttPort;
        private String MqttAdmin;
        private String MqttPassword;

        public void SetAllData() throws JSONException {
            this.LockerNo = Config.devId;
            this.MqttUrl = Config.mqttUrl;
            this.MqttPort = Config.mqttPort;
            this.MqttAdmin = Config.account;
            this.MqttPassword = Config.password;
        }

        public void SetLockerNo(String value) {
            this.LockerNo = value;
        }

        public String GetLockerNo() {
            return this.LockerNo;
        }

        public void SetMqttUrl(String value) {
            this.MqttUrl = value;
        }

        public String GetMqttUrl() {
            return this.MqttUrl;
        }

        public void SetMqttPort(String value) {
            this.MqttPort = value;
        }

        public String GetMqttPort() {
            return this.MqttPort;
        }

        public void SetMqttAdmin(String value) {
            this.MqttAdmin = value;
        }

        public String GetMqttAdmin() {
            return this.MqttAdmin;
        }

        public void SetMqttPassword(String value) {
            this.MqttPassword = value;
        }

        public String GetMqttPassword() {
            return this.MqttPassword;
        }

        @Override
        public String toString() {
            return "InitData{" +
                    "LockerNo='" + LockerNo + '\'' +
                    ", MqttUrl='" + MqttUrl + '\'' +
                    ", MqttPort='" + MqttPort + '\'' +
                    ", MqttAdmin='" + MqttAdmin + '\'' +
                    ", MqttPassword='" + MqttPassword + '\'' +
                    '}';
        }
    }
}
