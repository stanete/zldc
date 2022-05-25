package com.example.android.zldc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ErrorCode {
    /**
     * 仓门异常状态
     */
    public final static int AbnormalBucket = 1000;

    /**
     * 仓门状态
     */
    public final static int BucketState = 1001;
    /**
     * 烟雾报警
     */
    public final static int SmokeAlarm = 1101;
    /**
     * 充电器仓过温
     */
    public final static int ChargeTemperature = 1102;
    /**
     * 市电输入短路
     */
    public final static int ShortCircuitInput = 1109;
    /**
     * 市电输入过压
     */
    public final static int SevereOvervoltageInput = 1105;
    /**
     * 市电输入过流
     */
    public final static int MainsInputOvercurrent = 1107;
    /**
     * 急停故障
     */
    public final static int ScramFault = 1104;
    /**
     * 市电输入严重过压
     */
    public final static int OvervoltageMainsInput = 1108;
    /**
     * 市电输入欠压
     */
    public final static int UndervoltageMainsInput = 1106;
    /**
     * 充电器过温
     */
    public final static int OvertemperatureOfCharger = 1203;
    /**
     * 电池过温
     */
    public final static int BatteryThermal = 1205;
    /**
     * 充电时间过长
     */
    public final static int LongToCharge = 1206;

    /**
     * 充电器故障
     */
    public final static int ChargerFault = 1204;

    /**
     * 充电器保护
     */
    public final static int ChargerProtect = 1300;

    /**
     * 分控故障
     */
    public final static int ShareOfFault = 1207;
    /**
     * 充电异常
     */
    public final static int AbnormalCharge = 1208;
    /**
     * 电池通讯故障
     */
    public final static int BatteryCommunicationFailure = 1302;
    /**
     * 分控失联
     */
    public final static int ShareOfLost = 1301;

    //电池保护
    /**
     * 充电过压
     */
    public final static int ChargingOvervoltage = 2101;
    /**
     * 充电过流
     */
    public final static int ChargingFlow = 2102;
    /**
     * 放电⽋压
     */
    public final static int DischargeVoltage = 2103;
    /**
     * 放电过流
     */
    public final static int DischargeFlow = 2104;
    /**
     * 充电低温
     */
    public final static int ChargingLowTemperature = 2105;
    /**
     * 充电⾼温
     */
    public final static int ChargingHighYemperature = 2106;
    /**
     * 放电⾼温
     */
    public final static int DischargeHighTemperature = 2107;
    /**
     * 放电低温
     */
    public final static int DischargeTemperature = 2108;
    /**
     * 短路
     */
    public final static int ShortOut = 2109;

    //电池故障
    /**
     * 电芯失效
     */
    public final static int BatteriesFailure = 3101;
    /**
     * 电芯失衡
     */
    public final static int BatteriesImbalances = 3102;
    /**
     * 放电MOS损坏
     */
    public final static int DischargeMOSDamage = 3103;
    /**
     * IC损坏
     */
    public final static int ICIsDamaged = 3104;
    /**
     * 温度传感器损坏
     */
    public final static int DamageTemperatureSensor = 3105;
    /**
     * 充电MOS损坏
     */
    public final static int RechargeableMOSDamaged = 3106;

    //充电器保护
    /**
     * 输出反接
     */
    public final static int ChangeOutputReverse = 4008;
    /**
     * 输出短路
     */
    public final static int ChangeOutputShortCircuit = 4007;
    /**
     * 输出过流
     */
    public final static int ChangeOutputFlow = 4006;
    /**
     * 输出过压
     */
    public final static int ChangeOutputOvervoltage = 4005;
    /**
     * 输出欠压
     */
    public final static int ChangeOutputVoltage = 4004;
    /**
     * 输⼊过流
     */
    public final static int ChangeInputFlow = 4003;
    /**
     * 充电器过温1级
     */
    public final static int ChangeOverheatedLevel1 = 4002;
    /**
     * 充电器过温2级
     */
    public final static int ChangeOverheatedLevel2 = 4001;

    //充电器故障
    /**
     * 配置失效
     */
    public final static int ChangeConfigurationFailure = 4107;
    /**
     * 通讯故障
     */
    public final static int ChangeCommunicationFailure = 4106;
    /**
     * ⻛扇故障
     */
    public final static int ChangeFanFault = 4105;
    /**
     * 温度采样失效
     */
    public final static int ChangeTemperatureSamplingFailure = 4104;
    /**
     * 电压采样失效
     */
    public final static int ChangeVoltageSamplingFailure = 4103;
    /**
     * 电流采样失效
     */
    public final static int ChangeCurrentSamplingFailure = 4102;
    /**
     * 输出开关失效
     */
    public final static int ChangeOutputSwitchFailure = 4101;


    //固件升级失败原因
    //固件错误
    public final static int BatFirmwareError = 1401;
    //升级空间不⾜
    public final static int BatNotEnoughUpgradeSpace = 1402;
    //数据写⼊失败
    public final static int BatDataWriteFailed = 1403;
    //数据重复
    public final static int BatDataDuplication = 1404;
    //⽬标版本不匹配
    public final static int BatTargetVersionNotMatch = 1405;
    //通讯超时
    public final static int BatCommunicationTimeout = 1406;
    //电池拔出（仅⽀持带有K1K2信号电池）
    public final static int BatPullOutBattery = 1407;

    //充电器升级失败故障
    //固件错误
    public final static int ChaFirmwareError = 1501;
    //升级空间不⾜
    public final static int ChaNotEnoughUpgradeSpace = 1502;
    //数据写⼊失败
    public final static int ChaDataWriteFailed = 1503;
    //数据重复
    public final static int ChaDataDuplication = 1504;
    //⽬标版本不匹配
    public final static int ChaTargetVersionNotMatch = 1505;
    //通讯超时
    public final static int ChaCommunicationTimeout = 1506;

    //自定义故障（后台无上报的故障）
    /**
     * 输出电流超差
     */
    public final static int OutputCurrentOutOfTolerance = 10001;
    /**
     * 输入过压1级
     */
    public final static int ChangeInputOvervoltageLevel1 = 10002;
    /**
     * 输入过压2级
     */
    public final static int ChangeInputOvervoltageLevel2 = 10003;
    /**
     * 输入欠压1级
     */
    public final static int ChangeInputUndervoltageLevel1 = 10004;
    /**
     * 输入欠压2级
     */
    public final static int ChangeInputUndervoltageLevel2 = 10005;
    /**
     * 充电时间超限
     */
    public final static int ChangeTimeExceedsLimit = 10006;
    /**
     * 充电仓充电过流
     */
    public final static int ChargingBinOvercurrent = 10007;
    /**
     * 电池升级失败
     */
    public final static int BatteryUpgradeFailed = 10008;
    /**
     * 电池故障
     */
    public final static int BatteryFailure = 10009;
    /**
     * 电池保护
     */
    public final static int BatteryProtection = 10010;
    /**
     * 温差过⼤
     */
    public final static int BatteryTemperatureLarge = 10011;
    /**
     * 压差过⼤
     */
    public final static int BatteryPressureLarge = 10012;
    /**
     * 智能充电通信超时
     */
    public final static int BatteryCommunicationTimeout = 10013;
    /**
     * 放电通信超时
     */
    public final static int BatteryDischargeCommunicationTimeout = 10014;

    ArrayList error_1 = new ArrayList();

    ArrayList soundlist = new ArrayList();

    Map merror = new HashMap();
    //                                     1101      1102          1104           1108           1106
    String[] strings_1 = new String[]{"烟雾报警", "充电器仓过温", "相输入短路", "市电输入严重过压", "相输入过流",
            //1107      1205       10007新增     1105          1109         1203         1206
            "急停报警", "电池过温", "充电过流", "市电输入过压", "市电输入欠压", "充电器过温", "充电时间过长",
            //                  充电仓充电过流
            // 1301      10009       2105        10010        10008           1204          1300       1207      1208
            "分控失联", "电池故障", "充电低温保护", "电池保护", "电池升级失败", "充电器故障", "充电器保护", "分控故障", "充电异常"
            // 3101      3102        3103        3104        3105           3106             1302
            //"电芯失效","电芯失衡","放电MOS损坏","IC损坏","温度传感器损坏","充电MOS损坏", "电池通讯故障",
            //2101       2102       2103      2104       2105      2106       2107      2108     2109
            //"充电过压","充电过流","放电⽋压","放电过流","充电低温","充电⾼温","放电⾼温","放电低温","短路",
    };

    public int getErrorLeve(int error) {
        if (error >= 1101 && error <= 1109 || error == 1205 || error == 1203 || error == 1206 || error == 10007) {
            error = 1;
        } else
            error = 2;

        return error;
    }

    public int getCompare(int o, int t) {
        int r;

        int oi = 0;
        int ti = 0;
        String os = (String) merror.get(o);
        String ts = (String) merror.get(t);
        for (int i = 0; i < strings_1.length; i++) {
            if (strings_1[i].equals(os))
                oi = i;
            if (strings_1[i].equals(ts))
                ti = i;
        }

        if (oi == ti)
            r = 0;
        else if (oi < ti)
            r = 1;
        else
            r = -1;

        return r;
    }

    private void setmap(int i) {
        switch (i) {
            case 0:
                merror.put(SmokeAlarm, strings_1[i]);
                break;
            case 1:
                merror.put(ChargeTemperature, strings_1[i]);
                break;
            case 2:
                merror.put(ShortCircuitInput, strings_1[i]);
                break;
            case 3:
                merror.put(OvervoltageMainsInput, strings_1[i]);
                break;
            case 4:
                merror.put(MainsInputOvercurrent, strings_1[i]);
                break;
            case 5:
                merror.put(ScramFault, strings_1[i]);
                break;
            case 6:
                merror.put(BatteryThermal, strings_1[i]);
                break;
            case 7:
                merror.put(ChargingBinOvercurrent, strings_1[i]);
                break;
            case 8:
                merror.put(SevereOvervoltageInput, strings_1[i]);
                break;
            case 9:
                merror.put(UndervoltageMainsInput, strings_1[i]);
                break;
            case 10:
                merror.put(OvertemperatureOfCharger, strings_1[i]);
                break;
            case 11:
                merror.put(LongToCharge, strings_1[i]);
                break;
            case 12:
                merror.put(ShareOfLost, strings_1[i]);
                break;
            case 13:
                merror.put(BatteryFailure, strings_1[i]);
                break;
            case 14:
                merror.put(ChargingLowTemperature, strings_1[i]);
                break;
            case 15:
                merror.put(BatteryProtection, strings_1[i]);
                break;
            case 16:
                merror.put(BatteryUpgradeFailed, strings_1[i]);
                break;
            case 17:
                merror.put(ChargerFault, strings_1[i]);
                break;
            case 18:
                merror.put(ChargerProtect, strings_1[i]);
                break;
            case 19:
                merror.put(ShareOfFault, strings_1[i]);
                break;
            case 20:
                merror.put(AbnormalCharge, strings_1[i]);
                break;
        }
    }

    public ErrorCode() {
        for (int i = 0; i < strings_1.length; i++) {
            setmap(i);
            error_1.add(strings_1[i]);
        }
    }

    public String getErrorString(int code) {
        return merror.get(code).toString();
    }

    public int getErrorCode(String str) {
        int rkey = -1;
        for (Object key : merror.keySet()) {
            if (merror.get(key).equals(str)) {
                rkey = (int) key;
                break;
            }
        }
        return rkey;
    }

    //排序所有错误
    public ArrayList getSoundList(ArrayList v) {
        soundlist.clear();

        if (v.size() > 0) {
            int[] listint = listsort(v, error_1);
            for (int i = 0; i < listint.length; i++) {
                int j = listint[i];
                if (j != -1)
                    soundlist.add(error_1.get(listint[i]));
            }
        }

        return soundlist;
    }

    private int[] listsort(ArrayList v, ArrayList l) {
        int min;
        int[] sourcedata = new int[v.size()];
        for (int i = 0; i < sourcedata.length; i++)
            sourcedata[i] = -1;

        for (int i = 0; i < v.size(); i++) {
            Object o = merror.get(v.get(i));
            if (o != null) {
                String str = merror.get(v.get(i)).toString();
                if (l.contains(str)) {
                    sourcedata[i] = l.indexOf(str);
                }
            }
        }
        for (int i = 0; i < sourcedata.length - 1; i++) {
            for (int j = 0; j < sourcedata.length - 1 - i; j++) {
                if (sourcedata[j] == -1) {
                    min = sourcedata[j];
                    sourcedata[j] = sourcedata[j + 1];
                    sourcedata[j + 1] = min;
                } else {
                    if (sourcedata[j] > sourcedata[j + 1]) {
                        min = sourcedata[j + 1];
                        sourcedata[j + 1] = sourcedata[j];
                        sourcedata[j] = min;
                    }
                }
            }
        }
        return sourcedata;
    }
}
