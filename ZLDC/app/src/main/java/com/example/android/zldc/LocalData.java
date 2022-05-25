package com.example.android.zldc;

import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 本地数据，仅用于收集与主控的数据交互上报数据
 */
public class LocalData {
    /**
     * 本地数据
     */
    public static LocalData localData;
    /**
     * 是否有网络
     */
    public static boolean isNetworking=false;
    /**
     * 是否连接服务器
     */
    public static boolean isConnectService=false;
    /**
     * 是否正在升级
     */
    public static Updating updating;
    /**
     * 是否正在换电
     */
    public static boolean isExchanging=false;

    /**
     * 是否正在升级
     * @return
     */
    public static boolean getIsUpdating(){
        return LocalData.getUpdating().isUpdating();
    }

    public static ArrayList<String> fileUpdate_array = new ArrayList<>();
    /**
     * 记录是否开门
     */
    public static boolean[] isOpen=new boolean[8];

    /**
     * 获取本地数据
     *
     * @return
     */
    public static LocalData getLocalData() {
        if (localData == null) {
            localData = new LocalData();
        }
        return localData;
    }

    public class Updating{
        boolean appUpdating;
        boolean mainUpdating;
        boolean subUpdating;

        public boolean isAppUpdating() {
            return appUpdating;
        }

        public void setAppUpdating(boolean appUpdating) {
            this.appUpdating = appUpdating;
        }

        public boolean isMainUpdating() {
            return mainUpdating;
        }

        public void setMainUpdating(boolean mainUpdating) {
            this.mainUpdating = mainUpdating;
        }

        public boolean isSubUpdating() {
            return subUpdating;
        }

        public void setSubUpdating(boolean subUpdating) {
            this.subUpdating = subUpdating;
        }

        @Override
        public String toString() {
            return "Updating{" +
                    "appUpdating=" + appUpdating +
                    ", mainUpdating=" + mainUpdating +
                    ", subUpdating=" + subUpdating +
                    '}';
        }
        public boolean isUpdating(){
            return (this.appUpdating||this.mainUpdating||this.subUpdating)?true:false;
        }
    }
    public static Updating getUpdating(){
        if(updating==null){
            updating=new LocalData().new Updating();
        }
        return updating;
    }
    public static void setUpdating(Updating updatings){
        updating=updatings;
    }
    /**
     * 主控信息
     */
    private static MainData mainData;

    /**
     * 获取主控信息
     *
     * @return
     */
    public static MainData getMainData() {
        if (mainData == null) {
            mainData = new MainData();
        }
        return mainData;
    }

    /**
     * 主控信息
     */
    public static class MainData {
        /**
         * 主控协议号
         */
        private String mainProNum;
        /**
         * 主控软件版本号
         */
        private String mainSoftNum;
        /**
         * 故障状态信息
         */
        private boolean[] faultsState = new boolean[16];
        /**
         * 运行状态信息
         */
        private boolean[] runingState = new boolean[16];
        /**
         * 柜体温度
         */
        private String lockTemp;
        /**
         * 主控硬件版本
         */
        private int chVer;
        /**
         * 分控硬件版本
         */
        private int shVer;

        public String getMainProNum() {
            return mainProNum;
        }

        public void setMainProNum(String mainProNum) {
            this.mainProNum = mainProNum;
        }

        public String getMainSoftNum() {
            return mainSoftNum;
        }

        public void setMainSoftNum(String mainSoftNum) {
            this.mainSoftNum = mainSoftNum;
        }

        public boolean[] getFaultsState() {
            return faultsState;
        }

        public void setFaultsState(boolean[] faultsState) {
            this.faultsState = faultsState;
        }

        public boolean[] getRuningState() {
            return runingState;
        }

        public void setRuningState(boolean[] runingState) {
            this.runingState = runingState;
        }

        public String getLockTemp() {
            return lockTemp;
        }

        public void setLockTemp(String lockTemp) {
            this.lockTemp = lockTemp;
        }

        public int getChVer() {
            return chVer;
        }

        public void setChVer(int chVer) {
            this.chVer = chVer;
        }

        public int getShVer() {
            return shVer;
        }

        public void setShVer(int shVer) {
            this.shVer = shVer;
        }

        /**
         * 设置数据
         *
         * @param data 主控上报的数据
         */
        public void setData(byte[] data,int off) {
            int mpn, msn, lt;
            mpn = (data[off++] & 0xff);
            msn = (data[off++] & 0xff);
            setMainProNum("V" + mpn);
            setMainSoftNum("V" + msn);
            //故障状态信息
            binary_to_boolean(copyData(data, off++, 2), this.faultsState);
            //运行状态信息
            off++;
            binary_to_boolean(copyData(data, off++, 2), this.runingState);
            off++;
            lt = data[off++] & 0xff;
            int chVer = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
            setChVer(chVer);
            int shVer = (data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00);
            setShVer(shVer);
            setLockTemp(lt + "");
        }

        @Override
        public String toString() {
            return "MainData{" +
                    "mainProNum='" + mainProNum + '\'' +
                    ", mainSoftNum='" + mainSoftNum + '\'' +
                    ", faultsState=" + Arrays.toString(faultsState) +
                    ", runingState=" + Arrays.toString(runingState) +
                    ", lockTemp='" + lockTemp + '\'' +
                    ", chVer=" + chVer +
                    ", shVer=" + shVer +
                    '}';
        }
    }

    /**
     * 分控仓门偏移地址
     */
    private static int subOffset;
    /**
     * 分控仓门数据结构个数
     */
    private static int subNum;

    /**
     * 整柜分控状态信息
     */
    public static CopyOnWriteArrayList<SubData> subDataList;
    /**
     * 执行开仓时的所有分控状态
     */
    public static CopyOnWriteArrayList<SubData> lastSubDataList;

    public static int getSubOffset() {
        return subOffset;
    }

    public static void setSubOffset(int subOffset) {
        LocalData.subOffset = subOffset;
    }

    public static int getSubNum() {
        return subNum;
    }

    public static void setSubNum(int subNum) {
        LocalData.subNum = subNum;
    }

    /**
     * 设置整柜分控状态信息
     *
     * @param data   主控上报的分控状态信息数据包
     * @param list   用于接收的集合
     * @param subNum 分控状态信息数据结构个数
     */
    public static void setSubListData(byte[] data,int index ,int off,int subNum) {
        CopyOnWriteArrayList<LocalData.SubData> list = LocalData.getLocalData().getSubDataList();
        int offset = off;
        //单个分控状态信息大小
        int len = 8;
        //单仓状态变更
        if(subNum<list.size()){
            LocalData.SubData last = list.get(index);
            byte[] dd = copyData(data,offset,len);
            LocalData.SubData subData = new LocalData().new SubData().setData(dd);
            subData.setBoxId(index+1);
            list.remove(last);
            list.add(index,subData);
            LocalData.getLocalData().setLastSubDataList(list);
        }else{
            for (int i = 0; i < subNum; i++) {
                list.remove(i);
                byte[] dd = copyData(data,offset,len);
                LocalData.SubData subData = new LocalData().new SubData().setData(dd);
                subData.setBoxId(index+i+1);
                list.add(i,subData);
                offset +=len;
            }
        }
    }

    public static void setSubDataList(CopyOnWriteArrayList<LocalData.SubData> list){
        subDataList=list;
    }

    /**
     * 获取分控状态信息
     *
     * @return
     */
    public CopyOnWriteArrayList<SubData> getSubDataList() {
        if (subDataList == null||subDataList.size()==0) {
            subDataList = new CopyOnWriteArrayList<>();
            for (int i = 0; i < 8; i++) {
                subDataList.add(getSubData());
            }
        }
        return subDataList;
    }

    /**
     * 获取上一次
     * @return
     */
    public static CopyOnWriteArrayList<LocalData.SubData> getLastSubDataList(){
        if(lastSubDataList==null){
            lastSubDataList = new CopyOnWriteArrayList<>();
        }
        return lastSubDataList;
    }
    public static void setLastSubDataList(CopyOnWriteArrayList<LocalData.SubData> l){
        lastSubDataList=l;
    }
    /**
     * 分控信息
     */
    private static SubData subData;

    /**
     * 获取分控信息
     *
     * @return
     */
    public static SubData getSubData() {
        if (subData == null) {
            subData = new LocalData().new SubData();
        }
        return subData;
    }

    /**
     * 分控信息
     */
    public class SubData {
        /**
         * 仓位
         */
        private int boxId;
        /**
         * 分控协议号
         */
        private String subProNum;
        /**
         * 分控软件版本号
         */
        private String subSoftNum;
        /**
         * 仓门状态
         */
        private int boxStatus;
        /**
         * 仓定位（运营状态）
         */
        private int boxRunStatus;
        /**
         * 运行状态信息
         */
        private boolean[] runingState=new boolean[8];
        /**
         * 电池故障标识
         */
        private int batFaultTag;
        /**
         * 分控故障标识
         */
        private int subFaultTag;
        /**
         * 充电器故障标识
         */
        private int chgFaultTag;

        public String getSubProNum() {
            return subProNum;
        }

        public void setSubProNum(String subProNum) {
            this.subProNum = subProNum;
        }

        public String getSubSoftNum() {
            return subSoftNum;
        }

        public void setSubSoftNum(String subSoftNum) {
            this.subSoftNum = subSoftNum;
        }

        public int getBoxStatus() {
            return boxStatus;
        }

        public void setBoxStatus(int boxStatus) {
            this.boxStatus = boxStatus;
        }

        public int getBoxRunStatus() {
            return boxRunStatus;
        }

        public void setBoxRunStatus(int boxRunStatus) {
            this.boxRunStatus = boxRunStatus;
        }

        public boolean[] getRuningState() {
            return runingState;
        }

        public void setRuningState(boolean[] runingState) {
            this.runingState = runingState;
        }

        public int getBatFaultTag() {
            return batFaultTag;
        }

        public void setBatFaultTag(int batFaultTag) {
            this.batFaultTag = batFaultTag;
        }

        public int getSubFaultTag() {
            return subFaultTag;
        }

        public void setSubFaultTag(int subFaultTag) {
            this.subFaultTag = subFaultTag;
        }

        public int getChgFaultTag() {
            return chgFaultTag;
        }

        public void setChgFaultTag(int chgFaultTag) {
            this.chgFaultTag = chgFaultTag;
        }

        public int getBoxId() {
            return boxId;
        }

        public void setBoxId(int boxId) {
            this.boxId = boxId;
        }

        /**
         * 设置数据
         */
        public LocalData.SubData setData(byte[] data) {
            LocalData.SubData temp=new LocalData().new SubData();
            int off = 0;
            int mpn, msn, lt;
            mpn = (data[off++] & 0xFF);
            msn = (data[off++] & 0xFF);
            temp.setSubProNum("V" + mpn);
            temp.setSubSoftNum("V" + msn);
            temp.setBoxStatus(data[off++] & 0xff);
            temp.setBoxRunStatus(data[off++] & 0xff);
            //运行状态信息
            byte_to_boolean(new byte[]{data[off++]}, temp.runingState);
            //电池故障标识
            temp.setBatFaultTag(data[off++] & 0xff);
            //分控故障标识
            temp.setSubFaultTag(data[off++] & 0xff);
            //充电器故障标识
            temp.setChgFaultTag(data[off++] & 0xff);
            return temp;
        }

        @Override
        public String toString() {
            return "SubData{" +
                    "boxId=" + boxId +
                    ", subProNum='" + subProNum + '\'' +
                    ", subSoftNum='" + subSoftNum + '\'' +
                    ", boxStatus=" + boxStatus +
                    ", boxRunStatus=" + boxRunStatus +
                    ", runingState=" + Arrays.toString(runingState) +
                    ", batFaultTag=" + batFaultTag +
                    ", subFaultTag=" + subFaultTag +
                    ", chgFaultTag=" + chgFaultTag +
                    '}';
        }
    }

    /**
     * 下发配置是否成功
     */
    private static boolean[] configSuccess = new boolean[16];

    public static void setConfigSuccess(byte[] data) {
        binary_to_boolean(data, configSuccess);
    }

    /**
     * 获取下发配置是否成功数据
     *
     * @return
     */
    public static boolean[] getConfigSuccess() {
        return configSuccess;
    }

    /**
     * 开仓指令
     */
    public static OpenBoxComnd openBoxCommond;

    public static OpenBoxComnd getOpenBoxCommond() {
        if (openBoxCommond == null) {
            openBoxCommond = new LocalData().new OpenBoxComnd();
        }
        return openBoxCommond;
    }

    public static void setOpenBoxComnd(LocalData.OpenBoxComnd openboxcomnd){
        openBoxCommond=openboxcomnd;
    }

    /**
     * 开仓指令
     */
    public class OpenBoxComnd {
        /**
         * 仓门地址
         */
        private String boxNum;
        /**
         * 指令结果 1：执行中 2：操作繁忙
         */
        private int boxResult;
        /**
         * 电池是否在线
         */
        private boolean batIsOnline;
        /**
         * 电池sn码长度
         */
        private int batSnLen;
        /**
         * 电池sn码
         */
        private String batSn;

        public String getBoxNum() {
            return boxNum;
        }

        public void setBoxNum(String boxNum) {
            boxNum = boxNum;
        }

        public int getBoxResult() {
            return boxResult;
        }

        public void setBoxResult(int boxResult) {
            boxResult = boxResult;
        }

        public boolean getBatIsOnline() {
            return batIsOnline;
        }

        public void setBatIsOnline(boolean batIsOnline) {
            batIsOnline = batIsOnline;
        }

        public String getBatSn() {
            return batSn;
        }

        public void setBatSn(String batSn) {
            batSn = batSn;
        }

        public int getBatSnLen() {
            return batSnLen;
        }

        public void setBatSnLen(int batSnLen) {
            batSnLen = batSnLen;
        }

        public OpenBoxComnd setData(byte[] data,int sd) {
            OpenBoxComnd obc=new OpenBoxComnd();
            //[104, 12, 0, -123, 3, 0, 0, 0, 2, 0, -32, 22]
            int off = sd;
            int bn, br, snlen;
            bn = data[off++] & 0xff;//仓址
            br = data[off++] & 0xff;//执行结果
            boolean isonline = (data[off++] & 0xff) == 1; //电池是否在线
            if(isonline){
                snlen = data[off++] & 0xff;
                obc.setBatIsOnline(isonline);
                obc.setBatSnLen(snlen);
                //从第四个字节往后为sn的数据
                String sn = new String(copyData(data, 4, snlen));
                //默认编码
                obc.setBatSn(sn);
            }
            obc.setBoxNum(String.valueOf(bn + 1));
            obc.setBoxResult(br);
            return obc;
        }
    }

    /**
     * 开仓结果
     */
    public static OpenBoxResult openBoxResult;

    /**
     * 获取开仓结果
     *
     * @return
     */
    public static OpenBoxResult getOpenBoxResult() {
        if (openBoxResult == null) {
            openBoxResult = new LocalData().new OpenBoxResult();
        }
        return openBoxResult;
    }
    public static void setOpenBoxResult(OpenBoxResult obp){
        openBoxResult=obp;
    }
    /**
     * 开仓结果
     */
    public class OpenBoxResult {
        /**
         * 仓门地址
         */
        private int boxNum;
        /**
         * 状态机
         */
        private int boxState;
        /**
         * 仓门结果
         * -2:分控失联
         * 0:开仓成功
         * 1:分控未响应指令
         * 2:仓门故障
         * 3:分控执行开仓动作超时
         */
        private int boxResult;
        /**
         * 分控状态信息 (n)
         */
        private SubData subData;
        /**
         * 电池是否在线
         */
        private boolean isBatOnline;
        /**
         * 电池sn码
         */
        private String batSn;

        public OpenBoxResult setData(byte[] data,int sd) {
            int off=sd;
            OpenBoxResult obp=new OpenBoxResult();
            obp.setBoxNum((data[off++] & 0xff)+1);
            obp.setBoxState(data[off++] & 0xff);
            obp.setBoxResult(data[off++]);
            int mpn, msn;
            mpn = (data[off++] & 0xFF);
            msn = (data[off++] & 0xFF);
            subData = new LocalData().new SubData();
            subData.setSubProNum("V" + mpn);
            subData.setSubSoftNum("V" + msn);
            subData.setBoxStatus(data[off++] & 0xff);
            subData.setBoxRunStatus(data[off++] & 0xff);
            //运行状态信息
            byte_to_boolean(new byte[]{data[off++]}, subData.runingState);
            //电池故障标识
            subData.setBatFaultTag(data[off++] & 0xff);
            //分控故障标识
            subData.setSubFaultTag(data[off++] & 0xff);
            subData.setChgFaultTag(data[off++]&0xff);
            obp.setSubData(subData);
            boolean isBatOnline = (data[off++] & 0xff) == 1;
            obp.setBatOnline(isBatOnline);
            int snlen = data[off++] & 0xff;
            if(isBatOnline&&snlen!=0){
                String sn = new String(copyData(data, off, snlen));
                //默认编码
                obp.setBatSn(sn);
            }else{
                obp.setBatSn("");
            }
            return obp;
        }

        public int getBoxNum() {
            return boxNum;
        }

        public void setBoxNum(int boxNum) {
            this.boxNum = boxNum;
        }

        public int getBoxState() {
            return boxState;
        }

        public void setBoxState(int boxState) {
            this.boxState = boxState;
        }

        public int getBoxResult() {
            return boxResult;
        }

        public void setBoxResult(int boxResult) {
            this.boxResult = boxResult;
        }

        public SubData getSubData() {
            return subData;
        }

        public void setSubData(SubData subData) {
            this.subData = subData;
        }

        public boolean isBatOnline() {
            return isBatOnline;
        }

        public void setBatOnline(boolean batOnline) {
            isBatOnline = batOnline;
        }

        public String getBatSn() {
            return batSn;
        }

        public void setBatSn(String batSn) {
            this.batSn = batSn;
        }

        @Override
        public String toString() {
            return "OpenBoxResult{" +
                    "boxNum=" + boxNum +
                    ", boxState=" + boxState +
                    ", boxResult=" + boxResult +
                    ", subData=" + subData +
                    ", isBatOnline=" + isBatOnline +
                    ", batSn='" + batSn + '\'' +
                    '}';
        }
    }

    /**
     * 整柜电池信息
     */
    public static CopyOnWriteArrayList<Bms> bmsList;

    /**
     * 追加电池信息
     *
     * @param bms 电池信息
     */
    public static void addBms(Bms bms) {
        if (bmsList.size() == 0) {
            bmsList.add(bms);
            return;
        }
        boolean isExist = false;
        //当前追加仓位索引
        int index = 0;
        for (int i = 0; i < bmsList.size(); i++) {
            if (bms.getBoxNum() == bmsList.get(i).getBoxNum()) {
                index = i;
                isExist = true;
            }
        }
        if (isExist) {
            //仓门地址存在，直接剔除原始数据
            bmsList.remove(index);
        }
        bmsList.add(bms);
    }

    /**
     * 获取整柜电池信息
     *
     * @return
     */
    public CopyOnWriteArrayList<Bms> getBmsList() {
        if (bmsList == null) {
            bmsList = new CopyOnWriteArrayList<>();
        }
        return bmsList;
    }

    /**
     * bms
     */
    public class Bms {
        /**
         * 帧标识
         */
        int tag;
        /**
         * 仓门地址
         */
        int boxNum;
        /**
         * bms数据信息
         */
        BmsData bmsData;

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public int getBoxNum() {
            return boxNum;
        }

        public void setBoxNum(int boxNum) {
            this.boxNum = boxNum;
        }

        public BmsData getBmsData() {
            return bmsData;
        }

        public void setBmsData(BmsData bmsData) {
            this.bmsData = bmsData;
        }

        public void setData(byte[] data) {
            int off = 0;
            setTag((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00));
            setBoxNum(data[off++] & 0xff);
            LocalData.getLocalData().getBmsData().setData(copyData(data, 2, data.length - 2));
            setBmsData(LocalData.getLocalData().getBmsData());
        }

        @Override
        public String toString() {
            return "Bms{" +
                    "tag=" + tag +
                    ", boxNum=" + boxNum +
                    ", bmsData=" + bmsData +
                    '}';
        }
    }

    /**
     * bms数据信息
     */
    public BmsData bmsData;

    public BmsData getBmsData() {
        if (bmsData == null) {
            bmsData = new BmsData();
        }
        return bmsData;
    }

    /**
     * bms数据信息
     */
    public class BmsData {
        /**
         * 电池ID
         */
        private String id;
        /**
         * 最大充电电压 0.0 带一位小数
         */
        private float maxChargVol;
        /**
         * 最大充电电流 0.0 带一位小数
         */
        private float maxChargCur;
        /**
         * 充电机控制开关
         */
        private boolean contrSwitch;
        /**
         * 控制工作模式
         */
        private int contrModel;
        /**
         * 电池电压
         */
        private float vol;
        /**
         * 电池电流
         */
        private float cur;
        /**
         * 电池最高温度
         */
        private int maxTemp;
        /**
         * 电池最低温度
         */
        private int minTemp;
        /**
         * 电池SOC
         */
        private int soc;
        /**
         * 电池soh
         */
        private int soh;
        /**
         * 充电故障
         */
        private boolean[] chargInFault=new boolean[16];
        /**
         * 放电故障
         */
        private boolean[] chargOutFault=new boolean[16];
        /**
         * 最大放电电流
         */
        private float maxChargOutCur;
        /**
         * 通用故障
         */
        private boolean[] commonFault=new boolean[16];

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public float getMaxChargVol() {
            return maxChargVol;
        }

        public void setMaxChargVol(float maxChargVol) {
            this.maxChargVol = maxChargVol;
        }

        public float getMaxChargCur() {
            return maxChargCur;
        }

        public void setMaxChargCur(float maxChargCur) {
            this.maxChargCur = maxChargCur;
        }

        public boolean isContrSwitch() {
            return contrSwitch;
        }

        public void setContrSwitch(boolean contrSwitch) {
            this.contrSwitch = contrSwitch;
        }

        public int getContrModel() {
            return contrModel;
        }

        public void setContrModel(int contrModel) {
            this.contrModel = contrModel;
        }

        public float getVol() {
            return vol;
        }

        public void setVol(float vol) {
            this.vol = vol;
        }

        public float getCur() {
            return cur;
        }

        public void setCur(float cur) {
            this.cur = cur;
        }

        public int getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(int maxTemp) {
            this.maxTemp = maxTemp;
        }

        public int getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(int minTemp) {
            this.minTemp = minTemp;
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

        public boolean[] getChargInFault() {
            return chargInFault;
        }

        public void setChargInFault(boolean[] chargInFault) {
            this.chargInFault = chargInFault;
        }

        public boolean[] getChargOutFault() {
            return chargOutFault;
        }

        public void setChargOutFault(boolean[] chargOutFault) {
            this.chargOutFault = chargOutFault;
        }

        public float getMaxChargOutCur() {
            return maxChargOutCur;
        }

        public void setMaxChargOutCur(float maxChargOutCur) {
            this.maxChargOutCur = maxChargOutCur;
        }

        public boolean[] getCommonFault() {
            return commonFault;
        }

        public void setCommonFault(boolean[] commonFault) {
            this.commonFault = commonFault;
        }

        public BmsData setData(byte[] data) {
            BmsData bd=new BmsData();
            int off=0;
            //最大充电电压
            bd.setMaxChargVol(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00))*0.1f);
            //最大充电电流
            bd.setMaxChargCur(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00))*0.1f);
            //充电机控制开关
            bd.setContrSwitch((data[off++]&0xff)==1);
            //控制工作模式
            bd.setContrModel(data[off++]&0xff);
            //电池电压
            bd.setVol(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00))*0.1f);
            //电池电流
            bd.setCur(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00))*0.1f);
            //电池最高温度
            bd.setMaxTemp(data[off++]&0xff);
            //电池最低温度
            bd.setMinTemp(data[off++]&0xff);
            //soc
            bd.setSoc(data[off++]&0xff);
            //soh
            bd.setSoh(data[off++]&0xff);
            //充电故障
            binary_to_boolean(new byte[]{data[off++],data[off++]},bd.chargInFault);
//            bd.setChargInFault((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00));
            //放电故障
            binary_to_boolean(new byte[]{data[off++],data[off++]},bd.chargOutFault);
//            bd.setChargOutFault((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00));
            //最大放电电流
            bd.setMaxChargOutCur(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00))*0.1f);
            //通用故障
            binary_to_boolean(new byte[]{data[off++],data[off++]},bd.commonFault);
//            bd.setCommonFault((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00));
            off+=2;//保留两个字节
            byte[] batIds=new byte[16];
            System.arraycopy(data,off,batIds,0,16);
            String batId=new String(batIds);
            if(batId.contains("\\u0000")){
                batId="";
            }
            bd.setId(batId);
            return bd;
        }

        @Override
        public String toString() {
            return "BmsData{" +
                    "id='" + id + '\'' +
                    ", maxChargVol=" + maxChargVol +
                    ", maxChargCur=" + maxChargCur +
                    ", contrSwitch=" + contrSwitch +
                    ", contrModel=" + contrModel +
                    ", vol=" + vol +
                    ", cur=" + cur +
                    ", maxTemp=" + maxTemp +
                    ", minTemp=" + minTemp +
                    ", soc=" + soc +
                    ", soh=" + soh +
                    ", chargInFault=" + Arrays.toString(chargInFault) +
                    ", chargOutFault=" + Arrays.toString(chargOutFault) +
                    ", maxChargOutCur=" + maxChargOutCur +
                    ", commonFault=" + Arrays.toString(commonFault) +
                    '}';
        }
    }

    /**
     * 整柜充电器
     */
    public static CopyOnWriteArrayList<ChargeData> chargeList;

    /**
     * 追加充电器信息
     */
    public static void addCharge(ChargeData chargeData) {
        if (chargeList.size() == 0) {
            chargeList.add(chargeData);
        }
        boolean isExist = false;
        int index = 0;
        for (int i = 0; i < chargeList.size(); i++) {
            if (chargeData.getBoxNum() == chargeList.get(i).getBoxNum()) {
                isExist = true;
                index = i;
            }
        }
        if (isExist) {
            chargeList.remove(index);
        }
        chargeList.add(chargeData);
    }

    /**
     * 获取整柜充电器信息
     *
     * @return
     */
    public CopyOnWriteArrayList<ChargeData> getChargeList() {
        if (chargeList == null) {
            chargeList = new CopyOnWriteArrayList<>();
        }
        return chargeList;
    }

    /**
     * 充电器
     */
    public class ChargeData {
        /**
         * 帧标识
         */
        int tag;
        /**
         * 仓门地址
         */
        int boxNum;
        /**
         * 充电器信息
         */
        ChargeInfo chargeInfo;

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public int getBoxNum() {
            return boxNum;
        }

        public void setBoxNum(int boxNum) {
            this.boxNum = boxNum;
        }

        public ChargeInfo getChargeInfo() {
            return chargeInfo;
        }

        public void setChargeInfo(ChargeInfo chargeInfo) {
            this.chargeInfo = chargeInfo;
        }

        @Override
        public String toString() {
            return "ChargeData{" +
                    "tag=" + tag +
                    ", boxNum=" + boxNum +
                    ", chargeInfo=" + chargeInfo +
                    '}';
        }
    }


    /**
     * 充电器信息
     */
    public class ChargeInfo {
        /**
         * 设置电压 0.1
         */
        double stVol;
        /**
         * 设置电流 0.01
         */
        double stCur;
        /**
         * 开关机
         */
        boolean openClose;
        /**
         * 充电电流 0.01
         */
        double chargCur;
        /**
         * 电池电压 0.1
         */
        double batVol;
        /**
         * 输出电压
         */
        double outVol;
        /**
         * 故障信息
         */
        boolean[] faults = new boolean[16];
        /**
         * 状态信息
         */
        int status;

        public double getStVol() {
            return stVol;
        }

        public void setStVol(double stVol) {
            this.stVol = stVol;
        }

        public double getStCur() {
            return stCur;
        }

        public void setStCur(double stCur) {
            this.stCur = stCur;
        }

        public boolean isOpenClose() {
            return openClose;
        }

        public void setOpenClose(boolean openClose) {
            this.openClose = openClose;
        }

        public double getChargCur() {
            return chargCur;
        }

        public void setChargCur(double chargCur) {
            this.chargCur = chargCur;
        }

        public double getBatVol() {
            return batVol;
        }

        public void setBatVol(double batVol) {
            this.batVol = batVol;
        }

        public double getOutVol() {
            return outVol;
        }

        public void setOutVol(double outVol) {
            this.outVol = outVol;
        }

        public boolean[] isFaults() {
            return faults;
        }

        public void setFaults(boolean[] faults) {
            this.faults = faults;
        }

        public int isStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChargeInfo setData(byte[] data) {
            ChargeInfo ci=new ChargeInfo();
            int off = 0;
            //设置电压
            ci.setStVol(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1);
            //设置电流
            ci.setStCur(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.01);
            //开关机
            ci.setOpenClose(((data[off++] & 0xFF) + ((data[off++]) << 8) & 0xFF00) == 1);
            //充电电流
            ci.setChargCur(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.01);
            //电池电压
            ci.setBatVol(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1);
            //输出电压
            ci.setOutVol(((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00)) * 0.1);
            //故障信息
            binary_to_boolean(new byte[]{data[off++],data[off++]}, ci.faults);
            //状态信息
            ci.setStatus((data[off++] & 0xFF) + ((data[off++] << 8) & 0xFF00));
            return ci;
        }

        @Override
        public String toString() {
            return "ChargeInfo{" +
                    "stVol=" + stVol +
                    ", stCur=" + stCur +
                    ", openClose=" + openClose +
                    ", chargCur=" + chargCur +
                    ", batVol=" + batVol +
                    ", outVol=" + outVol +
                    ", faults=" + Arrays.toString(faults) +
                    ", status=" + status +
                    '}';
        }
    }

    //以下是公有数据转化方法

    /**
     * bit转bool
     *
     * @param data 待转化的数据
     * @param o    赋值的对象
     * @return
     */
    private static void binary_to_boolean(byte[] data, boolean[] o) {
        int size = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                byte jp = (byte) Math.pow(2, j);
                o[size++] = (data[i] & jp) == jp;
                if (size >= 16) return;
            }
        }
    }

    private static void byte_to_boolean(byte[] data, boolean[] o) {
        int size = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                byte jp = (byte) Math.pow(2, j);
                o[size++] = (data[i] & jp) == jp ? true : false;
                if(size >= subNum) return;
            }
        }
    }
    /**
     * byte[]数据拷贝
     *
     * @param data       待拷贝的原始数据
     * @param indexStart 开始拷贝位置
     * @param len        拷贝长度
     * @return
     */
    public static byte[] copyData(byte[] data, int indexStart, int len) {
        byte[] result = new byte[len];
        for (int i = indexStart, j = 0; j < len; i++, j++) {
            result[j] = data[i];
        }
        return result;
    }

    /**
     * 获取对应仓门的分控状态信息
     *
     * @param boxNum 仓门地址
     * @return
     */
    public static LocalData.SubData getSubData(String boxNum) {
        int boxNumStart = LocalData.getSubOffset();
        CopyOnWriteArrayList<LocalData.SubData> subdatas = LocalData.getLocalData().getSubDataList();
        LocalData.SubData subData = null;
        for (LocalData.SubData sd : subdatas) {
            if (boxNumStart == (Integer.valueOf(boxNum) - 1)) {
                subData = sd;
                break;
            }
            boxNumStart++;
        }
        return subData;
    }

    /**
     * 获取对应仓门的电池信息
     *
     * @param boxNum 仓门地址
     * @return
     */
    public static LocalData.Bms getBatData(String boxNum) {
        //在线电池集合
        CopyOnWriteArrayList<LocalData.Bms> list = LocalData.getLocalData().getBmsList();
        for (LocalData.Bms bms : list) {
            if (bms.getBoxNum() == Integer.valueOf(boxNum)) {
                return bms;
            }
        }
        return null;
    }

    public static LocalData.ChargeData getChargeData(String boxNum) {
        //在线充电器集合
        CopyOnWriteArrayList<LocalData.ChargeData> list = LocalData.getLocalData().getChargeList();
        for (LocalData.ChargeData charge : list) {
            if (charge.getBoxNum() == Integer.valueOf(boxNum)) {
                return charge;
            }
        }
        return null;
    }

    /**
     * 清理object,在APP退出后调用
     */
    public static void clearObject() {
        if (mainData != null) {
            mainData = null;
        }
        if (subDataList != null) {
            subDataList = null;
        }
        if (subOffset != 0) {
            subOffset = 0;
        }
        if (subNum != 0) {
            subNum = 0;
        }
        if (subData != null) {
            subData = null;
        }
        if (configSuccess != null) {
            configSuccess = null;
        }
        if (openBoxCommond != null) {
            openBoxCommond = null;
        }
        if (openBoxResult != null) {
            openBoxResult = null;
        }
        if (localData != null) {
            localData = null;
        }
    }

    /**
     * 记录开仓前后的soc
     */
    private static BatSocInPreAndAfter batSocInPreAndAfter;

    public static BatSocInPreAndAfter getBatSocInPreAndAfter(){
        if(batSocInPreAndAfter==null){
            batSocInPreAndAfter=new BatSocInPreAndAfter();
        }
        return batSocInPreAndAfter;
    }
    /**
     * 开仓前后的soc记录
     */
    public static class BatSocInPreAndAfter {
        private int per;
        private int after;
        private String perBatId;
        private String afterBatId;
        public int getPer() {
            return per;
        }

        public void setPer(int per) {
            this.per = per;
        }

        public int getAfter() {
            return after;
        }

        public void setAfter(int after) {
            this.after = after;
        }

        public String getPerBatId() {
            return perBatId;
        }

        public void setPerBatId(String perBatId) {
            this.perBatId = perBatId;
        }

        public String getAfterBatId() {
            return afterBatId;
        }

        public void setAfterBatId(String afterBatId) {
            this.afterBatId = afterBatId;
        }

        @Override
        public String toString() {
            return "BatSocInPreAndAfter{" +
                    "per=" + per +
                    ", after=" + after +
                    ", perBatId='" + perBatId + '\'' +
                    ", afterBatId='" + afterBatId + '\'' +
                    '}';
        }
    }

    public static HashCheck hashCheck;

    public HashCheck getHashCheck(){
        if(hashCheck==null){
            hashCheck=new HashCheck();
        }
        return hashCheck;
    }

    /**
     * 发收hash值校验
     */
    public class HashCheck{
        /**
         * 发布的hashCode
         */
        private CopyOnWriteArrayList<Integer> sendHash = new CopyOnWriteArrayList<>();
        /**
         * 收到的hashCode
         */
        private CopyOnWriteArrayList<Integer> revicvedHash = new CopyOnWriteArrayList<>();
        /**
         * 主题
         */
        private CopyOnWriteArrayList<String>  topics = new CopyOnWriteArrayList<>();
        /**
         * 主题消息
         */
        private CopyOnWriteArrayList<String>  messages = new CopyOnWriteArrayList<>();
        /**
         * 当前消息的重试次数
         */
        private CopyOnWriteArrayList<Integer> tryTimes = new CopyOnWriteArrayList<>();
        /**
         * 是否继续重试
         */
        private boolean isContinue=true;

        public void sendHash(Integer sH){
            Log.e("sendHashSize",sendHash.size()+"");
            if(sendHash.size()>5){
                return;
            }
            //hash值相同，说明两次发送的消息一致，不再继续做处理
            if(!sendHash.contains(sH) && (sH!=0)){
                sendHash.add(sH);
            }
        }

        public void revicvedHash(Integer rH){
            Log.e("revicvedHashSize",revicvedHash.size()+"");
            if(revicvedHash.size()>5){
                return;
            }
            if(!revicvedHash.contains(rH) && (rH!=0)){
                revicvedHash.add(rH);
            }
        }

        public void addMessages(String topic,String message){
            //消息内容不重复的，则添加进去
            if(!messages.contains(message)){
                topics.add(topic);
                messages.add(message);
            }
            sendHash(message.hashCode());
        }
        /**
         * TODO 1、发布主题时，加入sendHash码和主题及其内容
         *      2、收到回复时，加入revicvedHash，在与之前sendHash对应位置的一致时，移除主题和内容
         *      3、
         */
        public boolean checkIsReceived(Integer hashCode){
            //得到对应的位置
            int index = revicvedHash.indexOf(hashCode);
            //有收到发布消息的响应
            //是否存在收到的hash值
            boolean result = index!=-1?true:false;
            if(!result){
                return false;
            }
            //true: 说明有收到该hash的消息
            //false：说明没有收到该hash的消息，需要进行重发

            //获取对应位置的发送hash
            Integer sH = revicvedHash.get(index);
            //两者相等，直接移除对应位置的主题以及内容
            if(hashCode.equals(sH)){
                try{
                    revicvedHash.remove(index);
                    sendHash.remove(index);
                    topics.remove(index);
                    messages.remove(index);
                }catch(Exception e){
                    revicvedHash = new CopyOnWriteArrayList<>();
                    sendHash = new CopyOnWriteArrayList<>();
                    topics = new CopyOnWriteArrayList<>();
                    messages = new CopyOnWriteArrayList<>();
                    return true;
                }
                return true;
            }else{
                //两者不相等,只是移除收到里的hash
                revicvedHash.remove(index);
                return false;
            }
        }

        /**
         * 获取对应位置的主题
         * @param hash 哈希值
         * @return
         */
        public String getTopicOfHash(Integer hash){
            int index = sendHash.indexOf(hash);
            if(index==-1){
                return "";
            }
            return topics.get(index);
        }

        /**
         * 获取对应位置的消息
         * @param hash 哈希值
         * @return
         */
        public String getMessageOfHash(Integer hash){
            int index = sendHash.indexOf(hash);
            if(index==-1){
                return "";
            }
            return messages.get(index);
        }
        public CopyOnWriteArrayList<Integer> getSendHash() {
            return sendHash;
        }

        public void setSendHash(CopyOnWriteArrayList<Integer> sendHash) {
            this.sendHash = sendHash;
        }

        public CopyOnWriteArrayList<Integer> getRevicvedHash() {
            return revicvedHash;
        }

        public void setRevicvedHash(CopyOnWriteArrayList<Integer> revicvedHash) {
            this.revicvedHash = revicvedHash;
        }

        public CopyOnWriteArrayList<String> getTopics() {
            return topics;
        }

        public void setTopics(CopyOnWriteArrayList<String> topics) {
            this.topics = topics;
        }

        public CopyOnWriteArrayList<String> getMessages() {
            return messages;
        }

        public void setMessages(CopyOnWriteArrayList<String> messages) {
            this.messages = messages;
        }

        public CopyOnWriteArrayList<Integer> getTryTimes() {
            return tryTimes;
        }

        public void setTryTimes(CopyOnWriteArrayList<Integer> tryTimes) {
            this.tryTimes = tryTimes;
        }

        public boolean isContinue() {
            return isContinue;
        }

        public void setContinue(boolean aContinue) {
            isContinue = aContinue;
        }
    }


    /**
     * 告警信息
     */
    public static Set<AlertInfo> alertInfos;

    /**
     * 获取所有告警信息
     * @return
     */
    public static Set<AlertInfo> getAlertInfos(){
        if(alertInfos==null){
            alertInfos = new HashSet<>();
        }
        return alertInfos;
    }

    /**
     * 清理数据
     */
    public static void clearAlertInfos(){
        alertInfos = new HashSet<>();
    }

    /**
     * 追加故障信息
     */
    public static void addAlertInfo(AlertInfo ai){
        boolean isExist=false;
        //存在故障,则直接跳过
        for(AlertInfo a:alertInfos){
            if(ai.getSlotId()==a.getSlotId() && ai.getMessage().equals(a.getMessage()) && ai.getType().equals(a.getType())){
                isExist=true;
            }
        }
        if(!isExist){
            alertInfos.add(ai);
        }
    }
    /**
     *告警信息
     */
    public class AlertInfo{
        /**
         * 告警类型(Alarm or Error)
         */
        @JSONField(name="type")
        private String type;
        /**
         * 仓Id
         */
        @JSONField(name="slot_id")
        private Integer slotId;
        /**
         * 消息
         */
        @JSONField(name="message")
        private String message;
        /**
         * 时间
         */
        @JSONField(name="timestamp")
        private String timestamp;

        public AlertInfo() {
        }

        public AlertInfo(String type, Integer slotId, String message, String timestamp) {
            this.type = type;
            this.slotId = slotId;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getSlotId() {
            return slotId;
        }

        public void setSlotId(Integer slotId) {
            this.slotId = slotId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "AlertInfo{" +
                    "type='" + type + '\'' +
                    ", slotId=" + slotId +
                    ", message='" + message + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlertInfo alertInfo = (AlertInfo) o;
            return slotId == alertInfo.slotId && Objects.equals(type, alertInfo.type) && Objects.equals(message, alertInfo.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, slotId, message);
        }
    }
    /**
     * 分控掉线
     */
    public static boolean[] alertSubOffline = new boolean[8];
    /**
     * 分控其它故障
     */
    public static boolean[] alertSlotOTH = new boolean[8];
    /**
     * 电池故障
     */
    public static boolean[] alertBatFault = new boolean[8];
    /**
     * 充电器其它故障
     */
    public static boolean[] alertChargerOTH = new boolean[8];
    /**
     * 电池锁故障
     */
    public static boolean[] alertBatLock = new boolean[8];

    public static OpenBoxSuccess obs;

    public static OpenBoxSuccess getOpenBoxSuccess(){
        if(obs==null){
            obs=new LocalData().new OpenBoxSuccess();
        }
        return obs;
    }

    /**
     * 置为false
     */
    public static void clearOpenBoxSuccess(){
        if(obs!=null){
            obs.isIn=false;
            obs.isOut=false;
            obs.isOpen=false;
        }
    }
    public class OpenBoxSuccess{
        /**
         * 是否开仓
         */
        private boolean isOpen;
        /**
         * 是否插电池
         */
        private boolean isIn;
        /**
         * 是否拔电池
         */
        private boolean isOut;

        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen(boolean open) {
            isOpen = open;
        }

        public boolean isIn() {
            return isIn;
        }

        public void setIn(boolean in) {
            isIn = in;
        }

        public boolean isOut() {
            return isOut;
        }

        public void setOut(boolean out) {
            isOut = out;
        }

        public boolean isCloseSuccess(){
            int currentSlotInfo = LocalData.getCurrentSlotInfo();
            //开空仓，存在插电池，不存在拔电池
            if(currentSlotInfo==1){
                if((isOpen==true)&&(isIn==true)&&(isOut==false)){
                    return true;
                }
            }
            //开电池仓,存在拔电池，不存在插电池
            if(currentSlotInfo==2){
                if((isOpen==true)&&(isIn==false)&&(isOut==true)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "OpenBoxSuccess{" +
                    "isOpen=" + isOpen +
                    ", isIn=" + isIn +
                    ", isOut=" + isOut +
                    '}';
        }
    }

    /**
     * 当前执行的开仓事件
     * 1:空仓
     * 2:满仓
     *
     */
    public static int currentSlotInfo;

    public static int getCurrentSlotInfo() {
        if(currentSlotInfo==0){
            currentSlotInfo=1;
        }
        return currentSlotInfo;
    }

    public static void setCurrentSlotInfo(int csi) {
        currentSlotInfo = csi;
    }

    public static CopyOnWriteArrayList<String> checkSendMessage;

    public static CopyOnWriteArrayList<String> getCheckSendMessage(){
        if(checkSendMessage==null){
            checkSendMessage=new CopyOnWriteArrayList<>();
        }
        return checkSendMessage;
    }

    public static void removeCheckSendMessageByHash(Integer hashCode){
        if(checkSendMessage==null||checkSendMessage.size()==0){
            return;
        }
        Iterator<String> iterator=checkSendMessage.iterator();
        while (iterator.hasNext()){
            String message = iterator.next();
            if(hashCode.equals(message.hashCode())){
                iterator.remove();
            }
        }
    }

    public static void addCheckSendMessage(String msg){
        getCheckSendMessage().add(msg);
    }
}